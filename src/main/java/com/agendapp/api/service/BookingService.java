package com.agendapp.api.service;

import com.agendapp.api.controller.request.BookingRequest;
import com.agendapp.api.controller.request.BookingSearchRequest;
import com.agendapp.api.controller.response.BookingGridResponse;
import com.agendapp.api.controller.response.BookingResponse;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface BookingService {
    BookingResponse create(BookingRequest bookingRequest) throws Exception;

    Page<BookingGridResponse> findBookingGrid(UUID userId, BookingSearchRequest bookingSearchRequest);

    void cancelBooking(UUID bookingId);
}
