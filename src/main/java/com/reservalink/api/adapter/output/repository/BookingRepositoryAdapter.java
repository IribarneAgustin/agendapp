package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.adapter.output.repository.entity.BookingPackageEntity;
import com.reservalink.api.adapter.output.repository.entity.SlotTimeEntity;
import com.reservalink.api.application.output.BookingRepositoryPort;
import com.reservalink.api.domain.Booking;
import com.reservalink.api.domain.BookingStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public void save(Booking domain) {
        bookingJpaRepository.save(toEntity(domain));
    }

    @Override
    public List<Booking> findByBookingPackageIdAndStatus(String packageId, BookingStatus status) {
        return bookingJpaRepository.findByBookingPackageIdAndStatus(packageId, status)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void saveAll(List<Booking> bookings) {
        List<BookingEntity> entities = bookings.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        bookingJpaRepository.saveAll(entities);
    }

    private BookingEntity toEntity(Booking domain) {
        return BookingEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .phoneNumber(domain.getPhoneNumber())
                .name(domain.getName())
                .quantity(domain.getQuantity())
                .status(domain.getStatus())
                .enabled(domain.getEnabled())
                .slotTimeEntity(SlotTimeEntity.builder().id(domain.getSlotTimeId()).build())
                .bookingPackage(BookingPackageEntity.builder().id(domain.getBookingPackageId()).build())
                .build();
    }

}
