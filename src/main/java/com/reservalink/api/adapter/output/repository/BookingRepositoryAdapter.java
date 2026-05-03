package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.mapper.BookingRepositoryMapper;
import com.reservalink.api.application.output.BookingRepositoryPort;
import com.reservalink.api.domain.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BookingRepositoryAdapter implements BookingRepositoryPort {

    private final BookingRepository bookingJpaRepository;
    private final BookingRepositoryMapper bookingRepositoryMapper;

    @Override
    public Optional<Booking> findById(String bookingId) {
        return bookingJpaRepository.findByIdAndEnabledTrue(bookingId).map(bookingRepositoryMapper::toDomain);
    }

    @Override
    public List<Booking> findAllByIds(List<String> bookingIds) {
        return bookingJpaRepository.findAllByIdInAndEnabledTrue(bookingIds)
                .stream()
                .map(bookingRepositoryMapper::toDomain)
                .toList();
    }

    @Override
    public Integer findMaxBookingNumberByPhoneNumberAndUserId(String phoneNumber, String userId) {
        return bookingJpaRepository.findMaxBookingNumber(phoneNumber, userId);
    }

    @Override
    public boolean existsOverlappingBookingForResource(String resourceId, String id, LocalDateTime endDateTime, LocalDateTime startDateTime) {
        return bookingJpaRepository.existsOverlappingBookingForResource(resourceId, id, endDateTime, startDateTime);
    }
}
