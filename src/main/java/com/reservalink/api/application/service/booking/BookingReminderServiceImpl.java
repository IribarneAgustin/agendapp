package com.reservalink.api.application.service.booking;

import com.reservalink.api.application.output.BookingReminderJobRepositoryPort;
import com.reservalink.api.application.output.BookingRepositoryPort;
import com.reservalink.api.application.output.SubscriptionRepositoryPort;
import com.reservalink.api.application.service.feature.FeatureLifecycleService;
import com.reservalink.api.application.service.notification.NotificationChannel;
import com.reservalink.api.application.service.notification.NotificationService;
import com.reservalink.api.domain.Booking;
import com.reservalink.api.domain.BookingReminderJob;
import com.reservalink.api.domain.BookingReminderJobStatus;
import com.reservalink.api.domain.BookingStatus;
import com.reservalink.api.domain.FeatureName;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingReminderServiceImpl implements BookingReminderService {

    private static final int LIMIT_MINUTES_BEFORE_APPOINTMENT = 15;
    private static final int REMINDERS_THRESHOLD_HOURS = 24;
    private static final int SCHEDULED_REMINDER_MINUTE_BELOW_THRESHOLD = 5;
    private static final int SCHEDULED_REMINDER_HOUR = 12;
    private static final int SCHEDULED_REMINDER_MINUTE = 0;
    private static final int DAYS_BEFORE_REMINDER = 1;

    private final BookingReminderJobRepositoryPort reminderRepository;
    private final BookingRepositoryPort bookingRepositoryPort;
    private final NotificationService notificationService;
    private final FeatureLifecycleService featureLifecycleService;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;

    @Override
    public void sendReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<BookingReminderJob> jobs = reminderRepository.findEligibleReminders(now);

        if (jobs.isEmpty()) {
            log.debug("No reminders to send. Job Skipped.");
            return;
        }

        log.info("Processing {} Booking Reminders.", jobs.size());

        List<String> bookingIds = jobs.stream().map(BookingReminderJob::getBookingId).toList();
        Map<String, Booking> bookingsById = bookingRepositoryPort.findAllByIds(bookingIds).stream()
                .collect(Collectors.toMap(Booking::getId, Function.identity()));

        for (BookingReminderJob job : jobs) {
            try {
                Booking booking = bookingsById.get(job.getBookingId());
                NotificationChannel channel = job.getNotificationChannel();

                if (booking == null || booking.getStatus() != BookingStatus.CONFIRMED) {
                    log.info("Job {} cancelled: Booking invalid or not confirmed.", job.getId());
                    reminderRepository.updateStatus(job.getId(), BookingReminderJobStatus.CANCELLED, now);
                    continue;
                }

                String subId = subscriptionRepositoryPort.findActiveSubscriptionIdByBookingId(booking.getId());

                if (ObjectUtils.isEmpty(subId)) {
                    log.warn("Job {} skipped: No active subscription found for professional.", job.getId());
                    reminderRepository.updateStatus(job.getId(), BookingReminderJobStatus.CANCELLED, now);
                    continue;
                }

                if (NotificationChannel.WHATSAPP == channel) {
                    if (!featureLifecycleService.canUse(subId, FeatureName.WHATSAPP_NOTIFICATIONS)) {
                        log.warn("WhatsApp skipped for Job {}: Quota exceeded.", job.getId());
                        reminderRepository.updateStatus(job.getId(), BookingReminderJobStatus.CANCELLED, now);
                        continue;
                    }
                }

                notificationService.sendBookingReminder(booking, channel);

                reminderRepository.updateStatus(job.getId(), BookingReminderJobStatus.SENT, now);

                if (NotificationChannel.WHATSAPP == channel) {
                    featureLifecycleService.consume(subId, FeatureName.WHATSAPP_NOTIFICATIONS);
                }

            } catch (Exception e) {
                log.error("Failed to process {} reminder for Job ID: {}.", job.getNotificationChannel(), job.getId(), e);
                if (job.getReAttempts() + 1 <= 3) {
                    log.warn("Updating retry for job {}", job.getId());
                    reminderRepository.updateRetry(job.getId());
                } else {
                    log.warn("Max attempts reached for job {}. Marking as FAILED", job.getId());
                    reminderRepository.updateStatus(job.getId(), BookingReminderJobStatus.FAILED, now);
                }
            }
        }
    }

    @Override
    public void scheduleReminder(Booking booking) {
        LocalDateTime trigger = calculateTriggerTime(booking);

        if (trigger == null) {
            log.info("Booking {} too close to start time. Skipping reminder scheduling.", booking.getId());
            return;
        }
        try {
            saveJob(booking, trigger, NotificationChannel.EMAIL);
            String subscriptionId = subscriptionRepositoryPort.findActiveSubscriptionIdByBookingId(booking.getId());
            if (subscriptionId != null && featureLifecycleService.canUse(subscriptionId, FeatureName.WHATSAPP_NOTIFICATIONS)) {
                log.info("Scheduling WhatsApp reminder for Booking {}.", booking.getId());
                saveJob(booking, trigger, NotificationChannel.WHATSAPP);
            }
        } catch (Exception e) {
            log.error("Error scheduling reminders for BookingId: {}", booking.getId(), e);
        }
    }

    private void saveJob(Booking booking, LocalDateTime trigger, NotificationChannel channel) {
        BookingReminderJob job = BookingReminderJob.builder()
                .bookingId(booking.getId())
                .triggerDatetime(trigger)
                .status(BookingReminderJobStatus.PENDING)
                .notificationChannel(channel)
                .enabled(true)
                .reAttempts(0)
                .build();
        reminderRepository.save(job);
    }

    @Override
    public void cancelReminders(String bookingId) {
        List<BookingReminderJob> reminders = reminderRepository.findByBookingIdAndStatusIn(bookingId,
                List.of(BookingReminderJobStatus.RETRY, BookingReminderJobStatus.PENDING));
        if (!reminders.isEmpty()) {
            reminders.forEach(reminder -> {
                reminder.setEnabled(false);
                reminder.setStatus(BookingReminderJobStatus.DELETED);
                reminderRepository.save(reminder);
            });
        }
    }

    private LocalDateTime calculateTriggerTime(Booking booking) {
        LocalDateTime bookingStart = booking.getSlotTime().getStartDateTime();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime triggerTime = null;

        if (now.plusMinutes(LIMIT_MINUTES_BEFORE_APPOINTMENT).isAfter(bookingStart)) {
            return triggerTime;
        }

        Duration duration = Duration.between(now, bookingStart);
        if (duration.toHours() < REMINDERS_THRESHOLD_HOURS) {
            triggerTime = now.plusMinutes(SCHEDULED_REMINDER_MINUTE_BELOW_THRESHOLD);
        } else {
            triggerTime = bookingStart.toLocalDate()
                    .minusDays(DAYS_BEFORE_REMINDER)
                    .atTime(SCHEDULED_REMINDER_HOUR, SCHEDULED_REMINDER_MINUTE);
        }
        log.info("Scheduling reminders for Booking {} at {}.", booking.getId(), triggerTime);

        return triggerTime;
    }
}