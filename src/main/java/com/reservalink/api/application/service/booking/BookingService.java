package com.reservalink.api.application.service.booking;

import com.reservalink.api.adapter.input.controller.request.BookingRequest;
import com.reservalink.api.adapter.input.controller.request.BookingSearchRequest;
import com.reservalink.api.adapter.input.controller.response.BookingGridResponse;
import com.reservalink.api.adapter.input.controller.response.BookingResponse;
import org.springframework.data.domain.Page;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface BookingService {
    @Transactional(rollbackFor = Exception.class)
    BookingResponse create(BookingRequest bookingRequest, Boolean isAdmin) throws Exception;

    Page<BookingGridResponse> findBookingGrid(UUID userId, BookingSearchRequest bookingSearchRequest);

    @Transactional(rollbackFor = Exception.class)
    void cancelBooking(UUID bookingId);

    @Transactional(rollbackFor = Exception.class)
    void confirmBooking(String paymentExternalId);
}
