package com.reservalink.api.adapter.output.repository.entity;

import com.reservalink.api.application.service.notification.NotificationChannel;
import com.reservalink.api.domain.enums.BookingReminderJobStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "booking_reminder_job")
public class BookingReminderJobEntity extends PersistentObject {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private BookingEntity booking;

    @Column(name = "trigger_datetime")
    private LocalDateTime triggerDatetime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingReminderJobStatus status;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_channel", nullable = false)
    private NotificationChannel notificationChannel;

    @Column(name = "re_attempts")
    private Integer reAttempts;

}