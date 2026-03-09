package com.reservalink.api.application.service.booking;

import com.reservalink.api.adapter.input.controller.request.BookingPackageRequest;
import com.reservalink.api.adapter.input.controller.response.BookingResponse;
import com.reservalink.api.application.output.BookingPackageRepositoryPort;
import com.reservalink.api.application.output.BookingRepositoryPort;
import com.reservalink.api.application.output.PackageSessionRepositoryPort;
import com.reservalink.api.application.output.SlotTimeRepositoryPort;
import com.reservalink.api.application.service.payment.PaymentService;
import com.reservalink.api.domain.Booking;
import com.reservalink.api.domain.BookingPackage;
import com.reservalink.api.domain.BookingPackageStatus;
import com.reservalink.api.domain.BookingStatus;
import com.reservalink.api.domain.PackageSession;
import com.reservalink.api.domain.PackageSessionStatus;
import com.reservalink.api.domain.SlotTime;
import com.reservalink.api.exception.BusinessErrorCodes;
import com.reservalink.api.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingPackageServiceImpl implements BookingPackageService {

    private final BookingPackageRepositoryPort bookingPackageRepositoryPort;
    private final PackageSessionRepositoryPort packageSessionRepositoryPort;
    private final SlotTimeRepositoryPort slotTimeRepositoryPort;
    private final BookingRepositoryPort bookingRepositoryPort;
    private final PaymentService paymentService;

    @Override
    public BookingResponse createBookingPackage(BookingPackageRequest request) {
        log.info("Creating BookingPackage for offeringId: {}", request.getOfferingId());
        PackageSession packageSession = packageSessionRepositoryPort.findByOfferingId(request.getOfferingId())
                .orElseThrow(() -> new BusinessRuleException(BusinessErrorCodes.OFFERING_NOT_PACK_TYPE.name(),
                        Map.of("offeringId", request.getOfferingId())));
        boolean requiresPayment = packageSession.getAdvancePaymentPercentage() > 0;

        validatePackageSession(packageSession, request.getSlotTimeIds().size());

        BookingPackage bookingPackage = BookingPackage.builder()
                .packageSessionId(packageSession.getId())
                .sessionsTotal(packageSession.getSessionLimit())
                .sessionsUsed(requiresPayment ? 0 : request.getSlotTimeIds().size())
                .pricePaid(packageSession.getPrice())
                .customerEmail(request.getCustomerEmail())
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .status(requiresPayment ? BookingPackageStatus.PENDING : BookingPackageStatus.CONFIRMED)
                .enabled(true)
                .build();

        bookingPackage = bookingPackageRepositoryPort.save(bookingPackage);

        for (String slotTimeId : request.getSlotTimeIds()) {
            createBooking(slotTimeId, request, bookingPackage.getId(), requiresPayment);
        }

        String checkoutUrl = requiresPayment ? paymentService.createPackageCheckoutURL(bookingPackage, packageSession) : null;
        return BookingResponse.builder()
                .id(bookingPackage.getId())
                .checkoutURL(checkoutUrl)
                .build();
    }

    private void validatePackageSession(PackageSession session, int requestedSlots) {
        if (PackageSessionStatus.DISABLED.equals(session.getStatus())) {
            throw new BusinessRuleException(BusinessErrorCodes.PACKAGE_SESSION_LIMIT_EXCEEDED.name(),
                    Map.of("message", "Package session is disabled"));
        }
        if (requestedSlots != session.getSessionLimit()) {
            throw new BusinessRuleException(BusinessErrorCodes.PACKAGE_SESSION_LIMIT_EXCEEDED.name(),
                    Map.of("expected", session.getSessionLimit(), "actual", requestedSlots));
        }
    }

    private void createBooking(String slotId, BookingPackageRequest request, String packageId, boolean requiresPayment) {
        SlotTime slot = slotTimeRepositoryPort.findById(slotId)
                .orElseThrow(() -> new IllegalArgumentException("Slot time not found: " + slotId));

        Integer newCapacity = slot.getCapacityAvailable() - request.getQuantity();
        if (newCapacity < 0) {
            throw new BusinessRuleException(BusinessErrorCodes.SLOT_TIME_CAPACITY_EXCEEDED.name(),
                    Map.of("slotTimeId", slotId));
        }

        BookingStatus bookingStatus = BookingStatus.CONFIRMED;
        if (requiresPayment) {
            bookingStatus = BookingStatus.PENDING;
        } else {
            slot.setCapacityAvailable(newCapacity);
            slotTimeRepositoryPort.save(slot);
        }

        Booking booking = Booking.builder()
                .slotTimeId(slot.getId())
                .email(request.getCustomerEmail())
                .phoneNumber(request.getCustomerPhone())
                .name(request.getCustomerName())
                .quantity(request.getQuantity())
                .status(bookingStatus)
                .bookingPackageId(packageId)
                .enabled(true)
                .build();

        bookingRepositoryPort.save(booking);
    }

    @Override
    public void confirmPackageBookings(String bookingPackageId) {
        BookingPackage bookingPackage = bookingPackageRepositoryPort.findById(bookingPackageId)
                .orElseThrow(() -> new IllegalArgumentException("Not found: " + bookingPackageId));

        if (BookingPackageStatus.CONFIRMED.equals(bookingPackage.getStatus()) || bookingPackage.getExternalPaymentId() != null) {
            return;
        }

        bookingPackage.setStatus(BookingPackageStatus.CONFIRMED);
        bookingPackage.setExternalPaymentId("PACKAGE-" + bookingPackageId);
        bookingPackageRepositoryPort.save(bookingPackage);

        List<Booking> pendingBookings = bookingRepositoryPort.findByBookingPackageIdAndStatus(bookingPackageId, BookingStatus.PENDING);

        for (Booking booking : pendingBookings) {
            booking.setStatus(BookingStatus.CONFIRMED);

            slotTimeRepositoryPort.findById(booking.getSlotTimeId()).ifPresent(slot -> {
                slot.setCapacityAvailable(slot.getCapacityAvailable() - booking.getQuantity());
                slotTimeRepositoryPort.save(slot);
            });
        }

        bookingRepositoryPort.saveAll(pendingBookings);
    }
}
