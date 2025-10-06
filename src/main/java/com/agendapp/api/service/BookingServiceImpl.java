package com.agendapp.api.service;

import com.agendapp.api.controller.request.BookingRequest;
import com.agendapp.api.controller.request.BookingSearchRequest;
import com.agendapp.api.controller.response.BookingGridResponse;
import com.agendapp.api.controller.response.BookingResponse;
import com.agendapp.api.entity.Booking;
import com.agendapp.api.entity.BookingStatus;
import com.agendapp.api.entity.SlotTime;
import com.agendapp.api.repository.BookingRepository;
import com.agendapp.api.repository.SlotTimeRepository;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BookingServiceImpl implements BookingService {


    private final BookingRepository bookingRepository;
    private final SlotTimeRepository slotTimeRepository;
    private final ModelMapper modelMapper;

    public BookingServiceImpl(BookingRepository bookingRepository, SlotTimeRepository slotTimeRepository, ModelMapper modelMapper) {
        this.bookingRepository = bookingRepository;
        this.slotTimeRepository = slotTimeRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public BookingResponse create(BookingRequest bookingRequest) throws Exception {
        SlotTime slotTime = slotTimeRepository.findById(bookingRequest.getSlotTimeId().toString())
                .orElseThrow(() -> new IllegalArgumentException("The SlotTimeId: " + bookingRequest.getSlotTimeId() + " does not exists"));

        Integer newCapacity = slotTime.getCapacityAvailable() - 1;
        if(newCapacity < 0){
            throw new Exception("The slot is fully booked");
        }
        slotTime.setCapacityAvailable(newCapacity);
        slotTimeRepository.save(slotTime);

        Booking booking = Booking.builder()
                .active(true)
                .slotTime(slotTime)
                .name(bookingRequest.getName())
                .email(bookingRequest.getEmail())
                .phoneNumber(bookingRequest.getPhoneNumber())
                .build();

        //TODO Check user settings if need confirmation
        booking.setStatus(BookingStatus.CONFIRMED);

        bookingRepository.save(booking);

        //TODO: Get message template and send mail/push notification with CANCELATION link
        //booking.setNotified()

        return modelMapper.map(booking, BookingResponse.class);
    }

    @Override
    public Page<BookingGridResponse> findBookingGrid(BookingSearchRequest bookingSearchRequest) {
        Pageable pageable = PageRequest.of(bookingSearchRequest.getPageNumber(), bookingSearchRequest.getPageSize());

        Page<Booking> bookingPage = bookingRepository.findBookingGrid(
                bookingSearchRequest.getClientName(),
                bookingSearchRequest.getStartDate(),
                bookingSearchRequest.getMonth(),
                bookingSearchRequest.getOfferingId(),
                pageable
        );

        return bookingPage.map(b -> {
                    Double amountPaid = null;
                    Integer advancePaymentPercentage = b.getSlotTime().getOffering().getAdvancePaymentPercentage();
                    if(b.getSlotTime().getPrice() != null && advancePaymentPercentage != null && advancePaymentPercentage > 0) {
                        amountPaid = b.getSlotTime().getPrice() * (b.getSlotTime().getOffering().getAdvancePaymentPercentage() / 100);
                    }
                    return BookingGridResponse.builder()
                            .id(b.getId())
                            .clientName(b.getName())
                            .clientPhone(b.getPhoneNumber())
                            .clientEmail(b.getEmail())
                            .startDateTime(b.getSlotTime().getStartDateTime())
                            .endDateTime(b.getSlotTime().getEndDateTime())
                            .serviceName(b.getSlotTime().getOffering().getName())
                            .paid(amountPaid)
                            .status(b.getStatus())
                            .build();
                }
        );
    }

    @Override
    public void cancelBooking(UUID bookingId) {
        Booking bookingToCancel = bookingRepository.findById(bookingId.toString()).orElseThrow(
                () -> new IllegalArgumentException("The booking to cancel does not exists")
        );
        SlotTime slotTime = bookingToCancel.getSlotTime();

        if (slotTime == null || !slotTime.getActive() || slotTime.getEndDateTime().isBefore(LocalDateTime.now()) || bookingToCancel.getStatus().equals(BookingStatus.CANCELLED)) {
           throw new IllegalArgumentException("The booking to cancel is already cancelled or is related with invalid slot");
        }

        slotTime.setCapacityAvailable(slotTime.getCapacityAvailable() + 1);

        bookingToCancel.setActive(false);
        bookingToCancel.setStatus(BookingStatus.CANCELLED);

        slotTimeRepository.save(slotTime);
        bookingRepository.save(bookingToCancel);

        //TODO send notifications
    }
}
