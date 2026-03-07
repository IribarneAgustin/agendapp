package com.reservalink.api.application.service.bookingPackage;

import com.reservalink.api.adapter.input.controller.request.BookingPackageRequest;

public interface BookingPackageService {
    String createBookingPackage(BookingPackageRequest request);

    void confirmPackageBookings(String packageSessionId);
}
