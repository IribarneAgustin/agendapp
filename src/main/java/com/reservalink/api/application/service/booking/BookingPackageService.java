package com.reservalink.api.application.service.booking;

import com.reservalink.api.adapter.input.controller.request.BookingPackageRequest;
import com.reservalink.api.adapter.input.controller.response.BookingResponse;
import org.springframework.transaction.annotation.Transactional;

public interface BookingPackageService {
    @Transactional(rollbackFor = Exception.class)
    BookingResponse createBookingPackage(BookingPackageRequest request);

    @Transactional(rollbackFor = Exception.class)
    void confirmPackageBookings(String packageSessionId);
}
