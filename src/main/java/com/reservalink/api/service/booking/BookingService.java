package com.reservalink.api.service.booking;

import com.reservalink.api.controller.request.BookingRequest;
import com.reservalink.api.controller.request.BookingSearchRequest;
import com.reservalink.api.controller.response.BookingGridResponse;
import com.reservalink.api.controller.response.BookingResponse;
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
