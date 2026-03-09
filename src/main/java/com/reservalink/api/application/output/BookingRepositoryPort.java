package com.reservalink.api.application.output;

import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.domain.Booking;
import com.reservalink.api.domain.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepositoryPort {
    //FIXME Return domain
    List<BookingEntity> findAllByStatusAndBetweenStartAndEndDateTime(BookingStatus bookingStatus, LocalDateTime tomorrowStart, LocalDateTime tomorrowEnd);

    void save(Booking booking);

    List<Booking> findByBookingPackageIdAndStatus(String packageId, BookingStatus status);

    void saveAll(List<Booking> bookings);
}