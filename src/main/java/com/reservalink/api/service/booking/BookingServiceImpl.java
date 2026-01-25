package com.reservalink.api.service.booking;

import com.reservalink.api.controller.request.BookingRequest;
import com.reservalink.api.controller.request.BookingSearchRequest;
import com.reservalink.api.controller.response.BookingGridResponse;
import com.reservalink.api.controller.response.BookingResponse;
import com.reservalink.api.exception.BusinessErrorCodes;
import com.reservalink.api.exception.BusinessRuleException;
import com.reservalink.api.repository.PaymentRepository;
import com.reservalink.api.repository.entity.BookingEntity;
import com.reservalink.api.repository.entity.BookingPaymentEntity;
import com.reservalink.api.repository.entity.BookingStatus;
import com.reservalink.api.repository.entity.PaymentStatus;
import com.reservalink.api.repository.entity.ResourceEntity;
import com.reservalink.api.repository.entity.SlotTimeEntity;
import com.reservalink.api.repository.BookingRepository;
import com.reservalink.api.repository.SlotTimeRepository;
import com.reservalink.api.service.payment.PaymentService;
import com.reservalink.api.service.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class BookingServiceImpl implements BookingService {


    private final BookingRepository bookingRepository;
    private final SlotTimeRepository slotTimeRepository;
    private final ModelMapper modelMapper;
    private final NotificationService notificationService;
    private final PaymentService paymentService;
    private final PaymentRepository bookingPaymentRepository;

    public BookingServiceImpl(BookingRepository bookingRepository, SlotTimeRepository slotTimeRepository, ModelMapper modelMapper, NotificationService notificationService, PaymentService paymentService, PaymentRepository bookingPaymentRepository) {
        this.bookingRepository = bookingRepository;
        this.slotTimeRepository = slotTimeRepository;
        this.modelMapper = modelMapper;
        this.notificationService = notificationService;
        this.paymentService = paymentService;
        this.bookingPaymentRepository = bookingPaymentRepository;
    }

    @Override
    public BookingResponse create(BookingRequest bookingRequest, Boolean isAdmin) throws Exception {
        SlotTimeEntity slotTimeEntity = slotTimeRepository.findById(bookingRequest.getSlotTimeId().toString())
                .orElseThrow(() -> new IllegalArgumentException("The SlotTimeId: " + bookingRequest.getSlotTimeId() + " does not exists"));

        ResourceEntity resourceEntity = slotTimeEntity.getResourceEntity();

        boolean isResourceBusy =
                bookingRepository.existsOverlappingBookingForResource(
                        resourceEntity.getId(),
                        slotTimeEntity.getOfferingEntity().getId(),
                        slotTimeEntity.getEndDateTime(),
                        slotTimeEntity.getStartDateTime()
                );

        if (isResourceBusy) {
            throw new BusinessRuleException(
                    BusinessErrorCodes.RESOURCE_NOT_AVAILABLE.name()
            );
        }

        Integer newCapacity = slotTimeEntity.getCapacityAvailable() - bookingRequest.getQuantity();
        if(newCapacity < 0){
            log.warn("No capacity available for slotTimeId: {}. Booking creation ignored for {}", bookingRequest.getSlotTimeId(), bookingRequest.getEmail());
            throw new BusinessRuleException(BusinessErrorCodes.NO_CAPACITY_AVAILABLE.name());
        }

        BookingEntity bookingEntity = BookingEntity.builder()
                .enabled(true)
                .slotTimeEntity(slotTimeEntity)
                .name(bookingRequest.getName())
                .email(bookingRequest.getEmail())
                .phoneNumber(bookingRequest.getPhoneNumber())
                .quantity(bookingRequest.getQuantity())
                .build();

        if (paymentRequired(slotTimeEntity) && !isAdmin) {
            log.info("Creating new Booking with required payment");
            bookingEntity.setStatus(BookingStatus.PENDING);
            BookingEntity bookingEntitySaved = bookingRepository.saveAndFlush(bookingEntity);
            String checkoutURL = paymentService.createBookingCheckoutURL(bookingEntitySaved, bookingRequest.getQuantity());

            return BookingResponse.builder()
                    .checkoutURL(checkoutURL)
                    .build();
        } else {
            log.info("Creating new Booking without required payment");
            slotTimeEntity.setCapacityAvailable(newCapacity);
            slotTimeRepository.saveAndFlush(slotTimeEntity);

            bookingEntity.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.saveAndFlush(bookingEntity);
        }

        sendBookingConfirmedNotifications(bookingEntity);
        return modelMapper.map(bookingEntity, BookingResponse.class);
    }

    private void sendBookingConfirmedNotifications(BookingEntity bookingEntity) {
        try {
            notificationService.sendBookingConfirmed(bookingEntity);
        } catch (Exception e) {
            log.warn("Unexpected error sending new booking notifications.");
        }
    }

    private boolean paymentRequired(SlotTimeEntity slotTimeEntity) {
        return slotTimeEntity.getPrice() != null && slotTimeEntity.getPrice() > 0 && slotTimeEntity.getOfferingEntity().getAdvancePaymentPercentage() != null
                && slotTimeEntity.getOfferingEntity().getAdvancePaymentPercentage() > 0;
    }

    @Override
    public Page<BookingGridResponse> findBookingGrid(UUID userId, BookingSearchRequest bookingSearchRequest) {
        Pageable pageable = PageRequest.of(bookingSearchRequest.getPageNumber(), bookingSearchRequest.getPageSize());

        Page<BookingEntity> bookingPage = bookingRepository.findBookingGrid(
                userId.toString(),
                bookingSearchRequest.getClientName(),
                bookingSearchRequest.getStartDate(),
                bookingSearchRequest.getMonth(),
                bookingSearchRequest.getOfferingId(),
                pageable
        );

        return bookingPage.map(b -> {
                    Double amountPaid = null;
                    Integer advancePaymentPercentage = b.getSlotTimeEntity().getOfferingEntity().getAdvancePaymentPercentage();
                    if(b.getSlotTimeEntity().getPrice() != null && advancePaymentPercentage != null && advancePaymentPercentage > 0) {
                        amountPaid = b.getSlotTimeEntity().getPrice() * (b.getSlotTimeEntity().getOfferingEntity().getAdvancePaymentPercentage() / 100.0);
                    }
                    return BookingGridResponse.builder()
                            .id(b.getId())
                            .clientName(b.getName())
                            .clientPhone(b.getPhoneNumber())
                            .clientEmail(b.getEmail())
                            .startDateTime(b.getSlotTimeEntity().getStartDateTime())
                            .endDateTime(b.getSlotTimeEntity().getEndDateTime())
                            .serviceName(b.getSlotTimeEntity().getOfferingEntity().getName())
                            .paid(amountPaid)
                            .status(b.getStatus())
                            .quantity(b.getQuantity())
                            .build();
                }
        );
    }

    @Override
    public void cancelBooking(UUID bookingId) {
        BookingEntity bookingEntityToCancel = bookingRepository.findById(bookingId.toString()).orElseThrow(
                () -> new IllegalArgumentException("The booking to cancel does not exists")
        );
        SlotTimeEntity slotTimeEntity = bookingEntityToCancel.getSlotTimeEntity();

        if (!isValidStateToCancel(slotTimeEntity, bookingEntityToCancel)) {
           throw new IllegalArgumentException("The booking to cancel is already cancelled or is related with invalid slot");
        }

        slotTimeEntity.setCapacityAvailable(slotTimeEntity.getCapacityAvailable() + bookingEntityToCancel.getQuantity());

        bookingEntityToCancel.setStatus(BookingStatus.CANCELLED);

        slotTimeRepository.saveAndFlush(slotTimeEntity);
        bookingRepository.saveAndFlush(bookingEntityToCancel);

        try {
            notificationService.sendBookingCancelled(bookingEntityToCancel);
        } catch (Exception e) {
            log.warn("Unexpected error sending booking cancelled notifications.");
        }
    }

    private static boolean isValidStateToCancel(SlotTimeEntity slotTimeEntity, BookingEntity bookingEntityToCancel) {
        return slotTimeEntity != null && slotTimeEntity.getEnabled() && slotTimeEntity.getEndDateTime().isAfter(LocalDateTime.now())
                && !bookingEntityToCancel.getStatus().equals(BookingStatus.CANCELLED);
    }

    @Override
    public void confirmBooking(String externalPaymentId) {
        BookingPaymentEntity bookingPaymentEntity = bookingPaymentRepository
                .findByExternalId(externalPaymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found for reference: " + externalPaymentId));
        if (bookingPaymentEntity.getPaymentStatus().equals(PaymentStatus.COMPLETED)
                && !bookingPaymentEntity.getBookingEntity().getStatus().equals(BookingStatus.CONFIRMED)) {
            BookingEntity bookingEntity = bookingPaymentEntity.getBookingEntity();
            SlotTimeEntity slotTimeEntity = bookingEntity.getSlotTimeEntity();

            int quantityBooked = bookingEntity.getQuantity();
            int available = slotTimeEntity.getCapacityAvailable();

            slotTimeEntity.setCapacityAvailable(available - quantityBooked);
            slotTimeRepository.save(slotTimeEntity);

            bookingEntity.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(bookingEntity);

            sendBookingConfirmedNotifications(bookingEntity);
        } else {
            log.warn("Payment was not completed successfully for the externalPaymentId: {}. The booking was not confirmed.", externalPaymentId );
        }
    }

}
