package com.reservalink.api.application.service.bookingPackage;

import com.reservalink.api.adapter.input.controller.request.BookingPackageRequest;
import com.reservalink.api.adapter.output.repository.BookingRepository;
import com.reservalink.api.adapter.output.repository.SlotTimeRepository;
import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.adapter.output.repository.entity.BookingPackageEntity;
import com.reservalink.api.adapter.output.repository.entity.PackageSessionEntity;
import com.reservalink.api.adapter.output.repository.entity.SlotTimeEntity;
import com.reservalink.api.application.output.BookingPackageRepositoryPort;
import com.reservalink.api.application.output.PackageSessionRepositoryPort;
import com.reservalink.api.application.service.payment.PaymentService;
import com.reservalink.api.domain.BookingPackageStatus;
import com.reservalink.api.domain.BookingStatus;
import com.reservalink.api.domain.PackageSessionStatus;
import com.reservalink.api.exception.BusinessErrorCodes;
import com.reservalink.api.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingPackageServiceImpl implements BookingPackageService {

    private final BookingPackageRepositoryPort bookingPackageRepositoryPort;
    private final PackageSessionRepositoryPort packageSessionRepositoryPort;
    private final SlotTimeRepository slotTimeRepository;
    private final BookingRepository bookingRepository;
    private final PaymentService paymentService;

    @Override
    @Transactional
    public String createBookingPackage(BookingPackageRequest request) {
        log.info("Creating BookingPackage for offeringId: {} with {} slots", request.getOfferingId(),
                request.getSlotTimeIds().size());

        PackageSessionEntity packageSession = packageSessionRepositoryPort.findByOfferingId(request.getOfferingId())
                .orElseThrow(() -> new BusinessRuleException(BusinessErrorCodes.OFFERING_NOT_PACK_TYPE.name(),
                        Map.of("offeringId", request.getOfferingId())));

        if (PackageSessionStatus.DISABLED.equals(packageSession.getStatus())) {
            throw new BusinessRuleException(BusinessErrorCodes.PACKAGE_SESSION_LIMIT_EXCEEDED.name(),
                    Map.of("message", "Package session is disabled"));
        }

        if (request.getSlotTimeIds().size() != packageSession.getSessionLimit()) {
            throw new BusinessRuleException(BusinessErrorCodes.PACKAGE_SESSION_LIMIT_EXCEEDED.name(),
                    Map.of(
                            "expected", packageSession.getSessionLimit(),
                            "actual", request.getSlotTimeIds().size()));
        }

        BookingPackageEntity bookingPackage = BookingPackageEntity.builder()
                .packageSession(packageSession)
                .customerEmail(request.getCustomerEmail())
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .sessionsUsed(0)
                .status(BookingPackageStatus.PENDING_PAYMENT)
                .enabled(true)
                .build();

        bookingPackage = bookingPackageRepositoryPort.save(bookingPackage);

        for (String slotTimeId : request.getSlotTimeIds()) {
            SlotTimeEntity slotTime = slotTimeRepository.findById(slotTimeId)
                    .orElseThrow(() -> new IllegalArgumentException("Slot time not found: " + slotTimeId));

            if (slotTime.getCapacityAvailable() < 1) {
                log.error("Slot {} does not have enough capacity.", slotTimeId);
                throw new BusinessRuleException(BusinessErrorCodes.SLOT_TIME_CAPACITY_EXCEEDED.name(),
                        Map.of("slotTimeId", slotTimeId));
            }

            BookingEntity bookingEntity = BookingEntity.builder()
                    .slotTimeEntity(slotTime)
                    .email(request.getCustomerEmail())
                    .phoneNumber(request.getCustomerPhone())
                    .name(request.getCustomerName())
                    .quantity(1)
                    .status(BookingStatus.PENDING)
                    .bookingPackage(bookingPackage)
                    .enabled(true)
                    .build();

            bookingRepository.save(bookingEntity);
        }

        String checkoutUrl = paymentService.createPackageCheckoutURL(bookingPackage, packageSession.getPrice());
        log.info("Checkout URL generated: {}", checkoutUrl);
        return checkoutUrl;
    }

    @Override
    @Transactional
    public void confirmPackageBookings(String bookingPackageId) {
        log.info("Confirming package bookings for bookingPackageId: {}", bookingPackageId);
        BookingPackageEntity bookingPackage = bookingPackageRepositoryPort.findById(bookingPackageId)
                .orElseThrow(
                        () -> new IllegalArgumentException("BookingPackage not found with id: " + bookingPackageId));

        if (BookingPackageStatus.ACTIVE.equals(bookingPackage.getStatus())
                || bookingPackage.getExternalPaymentId() != null) {
            log.info("BookingPackage {} is already confirmed.", bookingPackageId);
            return;
        }

        bookingPackage.setStatus(BookingPackageStatus.ACTIVE);
        bookingPackage.setExternalPaymentId("PACKAGE-" + bookingPackageId);
        bookingPackageRepositoryPort.save(bookingPackage);

        List<BookingEntity> pendingBookings = bookingRepository.findByBookingPackageIdAndStatus(bookingPackageId,
                BookingStatus.PENDING);

        for (BookingEntity booking : pendingBookings) {
            booking.setStatus(BookingStatus.CONFIRMED);

            SlotTimeEntity slot = booking.getSlotTimeEntity();
            slot.setCapacityAvailable(slot.getCapacityAvailable() - booking.getQuantity());
            slotTimeRepository.save(slot);
        }

        bookingRepository.saveAll(pendingBookings);
        log.info("Successfully confirmed {} bookings for package {}", pendingBookings.size(), bookingPackageId);
    }
}
