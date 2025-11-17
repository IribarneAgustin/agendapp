package com.agendapp.api.service.booking;

import com.agendapp.api.controller.request.BookingRequest;
import com.agendapp.api.controller.request.BookingSearchRequest;
import com.agendapp.api.controller.response.BookingGridResponse;
import com.agendapp.api.controller.response.BookingResponse;
import com.agendapp.api.repository.entity.BookingEntity;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface BookingService {
    @Transactional(rollbackFor = Exception.class)
    BookingResponse create(BookingRequest bookingRequest) throws Exception;

    Page<BookingGridResponse> findBookingGrid(UUID userId, BookingSearchRequest bookingSearchRequest);

    void cancelBooking(UUID bookingId);

    @Transactional(rollbackFor = Exception.class)
    void confirmBooking(String paymentExternalId);
}
