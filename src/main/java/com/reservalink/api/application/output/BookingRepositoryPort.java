package com.reservalink.api.application.output;

import com.reservalink.api.domain.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepositoryPort {

    Optional<Booking> findById(String bookingId);

    List<Booking> findAllByIds(List<String> bookingIds);

    Integer findMaxBookingNumberByPhoneNumberAndUserId(String email, String userId);
}