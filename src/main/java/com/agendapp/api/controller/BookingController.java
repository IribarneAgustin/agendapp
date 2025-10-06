package com.agendapp.api.controller;

import com.agendapp.api.controller.request.BookingRequest;
import com.agendapp.api.controller.request.BookingSearchRequest;
import com.agendapp.api.controller.response.BookingGridResponse;
import com.agendapp.api.controller.response.BookingResponse;
import com.agendapp.api.service.BookingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    /*
    *
    * This Endpoint Assumes that if payment is required, it was made successfully
    *
    * */
    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest bookingRequest) throws Exception {
        BookingResponse response = bookingService.create(bookingRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<BookingGridResponse>> findAll(@PathVariable UUID userId, @Valid @ModelAttribute BookingSearchRequest bookingSearchRequest) {
        log.info("Request to fetch bookings for the user {} received", userId);
        Page<BookingGridResponse> response = bookingService.findBookingGrid(bookingSearchRequest);
        log.info("Bookings fetched successfully");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PatchMapping("/{bookingId}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelBooking(@PathVariable UUID bookingId) {
        log.info("Request to cancel the following booking received: {}", bookingId);
        bookingService.cancelBooking(bookingId);
        log.info("Booking {} cancelled successfully", bookingId);
    }


}
