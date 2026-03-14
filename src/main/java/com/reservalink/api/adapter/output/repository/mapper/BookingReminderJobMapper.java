package com.reservalink.api.adapter.output.repository.mapper;

import com.reservalink.api.adapter.output.repository.entity.BookingReminderJobEntity;
import com.reservalink.api.domain.BookingReminderJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingReminderJobMapper {

    public BookingReminderJob toDomain(BookingReminderJobEntity entity) {
        if (entity == null) {
            return null;
        }

        return BookingReminderJob.builder()
                .id(entity.getId())
                .bookingId(entity.getBooking().getId())
                .triggerDatetime(entity.getTriggerDatetime())
                .status(entity.getStatus())
                .processedAt(entity.getProcessedAt())
                .enabled(entity.getEnabled())
                .notificationChannel(entity.getNotificationChannel())
                .reAttempts(entity.getReAttempts())
                .build();
    }
}