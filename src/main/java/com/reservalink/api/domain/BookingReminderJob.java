package com.reservalink.api.domain;

import com.reservalink.api.application.service.notification.NotificationChannel;
import com.reservalink.api.domain.enums.BookingReminderJobStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
public class BookingReminderJob {
    private String id;
    private String bookingId;
    private LocalDateTime triggerDatetime;
    private BookingReminderJobStatus status;
    private LocalDateTime processedAt;
    private NotificationChannel notificationChannel;
    private Boolean enabled;
    private Integer reAttempts;
}