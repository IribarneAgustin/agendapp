package com.reservalink.api.adapter.input.controller;

import com.reservalink.api.adapter.input.controller.request.BookingPackageRequest;
import com.reservalink.api.adapter.input.controller.response.BookingResponse;
import com.reservalink.api.application.service.booking.BookingPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/booking-package")
@RequiredArgsConstructor
public class BookingPackageController {

    private final BookingPackageService bookingPackageService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBookingPackage(@Valid @RequestBody BookingPackageRequest request) {
        log.info("Received request to create Booking Package for offeringId: {} with {} slots", request.getOfferingId(), request.getSlotTimeIds().size());
        BookingResponse response = bookingPackageService.createBookingPackage(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
