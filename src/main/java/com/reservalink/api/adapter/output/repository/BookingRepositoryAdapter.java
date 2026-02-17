package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.application.output.BookingRepositoryPort;
import com.reservalink.api.domain.Booking;
import com.reservalink.api.domain.BookingStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class BookingRepositoryAdapter implements BookingRepositoryPort {

    private final BookingRepository bookingJpaRepository;

    public BookingRepositoryAdapter(BookingRepository bookingJpaRepository) {
        this.bookingJpaRepository = bookingJpaRepository;
    }

    @Override
    public List<BookingEntity> findAllByStatusAndBetweenStartAndEndDateTime(BookingStatus bookingStatus, LocalDateTime tomorrowStart, LocalDateTime tomorrowEnd) {
        return bookingJpaRepository.findBySlotTimeEntityStartDateTimeBetweenAndStatusAndEnabledTrue(tomorrowStart, tomorrowEnd, bookingStatus);
        /*TODO refactor this way:
        return bookings.stream()
                .filter(Objects::nonNull)
                .map(this::toDomain)
                .collect(Collectors.toList());*/
    }

    private Booking toDomain(BookingEntity entity) {
        if (entity == null) {
            return null;
        }
        return Booking.builder()
                .id(entity.getId())
                .slotTimeId(entity.getSlotTimeEntity() != null ? entity.getSlotTimeEntity().getId() : null)
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .name(entity.getName())
                .quantity(entity.getQuantity())
                .status(entity.getStatus())
                .build();
    }
}
