package com.reservalink.api.controller;

import com.reservalink.api.controller.request.BookingRequest;
import com.reservalink.api.controller.request.BookingSearchRequest;
import com.reservalink.api.controller.response.BookingGridResponse;
import com.reservalink.api.controller.response.BookingResponse;
import com.reservalink.api.service.booking.BookingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest bookingRequest) throws Exception {
        log.info("Request received to create a new Booking. Email: {}, SlotTimeId: {}", bookingRequest.getEmail(), bookingRequest.getSlotTimeId());
        BookingResponse response = bookingService.create(bookingRequest, false);
        log.info("Booking created successfully");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }

    @PostMapping("/admin")
    public ResponseEntity<BookingResponse> createAsAdmin(@Valid @RequestBody BookingRequest bookingRequest) throws Exception {
        BookingResponse response = bookingService.create(bookingRequest, true);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);

    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<BookingGridResponse>> findAll(@PathVariable UUID userId, @Valid @ModelAttribute BookingSearchRequest bookingSearchRequest) {
        log.info("Request to fetch bookings for the user {} received", userId);
        Page<BookingGridResponse> response = bookingService.findBookingGrid(userId, bookingSearchRequest);
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

    @GetMapping("/{bookingId}/cancel")
    public String cancelBookingFromLink(@PathVariable UUID bookingId) {
        log.info("Request to cancel the booking from the link. BookingId: {}", bookingId);
        bookingService.cancelBooking(bookingId);
        return "redirect:/public/cancelled-success.html";
    }


}
