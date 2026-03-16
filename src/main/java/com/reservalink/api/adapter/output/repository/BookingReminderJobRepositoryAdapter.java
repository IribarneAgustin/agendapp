package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.adapter.output.repository.entity.BookingReminderJobEntity;
import com.reservalink.api.adapter.output.repository.mapper.BookingReminderJobMapper;
import com.reservalink.api.application.output.BookingReminderJobRepositoryPort;
import com.reservalink.api.domain.BookingReminderJob;
import com.reservalink.api.domain.BookingReminderJobStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingReminderJobRepositoryAdapter implements BookingReminderJobRepositoryPort {

    private final BookingReminderJobJpaRepository repository;
    private final BookingReminderJobMapper bookingReminderJobMapper;

    @Override
    public BookingReminderJob save(BookingReminderJob job) {
        BookingReminderJobEntity entity = BookingReminderJobEntity.builder()
                .id(job.getId() != null ? job.getId() : UUID.randomUUID().toString())
                .booking(BookingEntity.builder().id(job.getBookingId()).build())
                .triggerDatetime(job.getTriggerDatetime())
                .status(job.getStatus())
                .processedAt(job.getProcessedAt())
                .enabled(job.getEnabled())
                .notificationChannel(job.getNotificationChannel())
                .reAttempts(job.getReAttempts())
                .build();

        BookingReminderJobEntity saved = repository.saveAndFlush(entity);

        return bookingReminderJobMapper.toDomain(saved);
    }

    @Override
    public List<BookingReminderJob> findEligibleReminders(LocalDateTime triggerTime) {
        return repository
                .findTop100ByStatusInAndTriggerDatetimeLessThanEqualOrderByTriggerDatetimeAsc(List.of(BookingReminderJobStatus.PENDING,BookingReminderJobStatus.RETRY), triggerTime)
                .stream()
                .map(bookingReminderJobMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void updateStatus(String jobId, BookingReminderJobStatus status, LocalDateTime processedAt) {
        BookingReminderJobEntity entity = repository.findById(jobId).orElseThrow();
        entity.setStatus(status);
        entity.setProcessedAt(processedAt);
        repository.saveAndFlush(entity);
    }

    @Override
    public void updateRetry(String jobId) {
        BookingReminderJobEntity entity = repository.findById(jobId).orElseThrow();
        entity.setStatus(BookingReminderJobStatus.RETRY);
        entity.setReAttempts(entity.getReAttempts() + 1);
        repository.saveAndFlush(entity);
    }

    @Override
    public List<BookingReminderJob> findByBookingIdAndStatusIn(String bookingId, List<BookingReminderJobStatus> statusList) {
        return Optional.ofNullable(repository.findAllByEnabledTrueAndBooking_IdAndStatusIn(bookingId, statusList)).orElse(Collections.emptyList())
                .stream()
                .map(bookingReminderJobMapper::toDomain)
                .collect(Collectors.toList());
    }

}