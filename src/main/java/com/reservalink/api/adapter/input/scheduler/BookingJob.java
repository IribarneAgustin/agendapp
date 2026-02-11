package com.reservalink.api.adapter.input.scheduler;

import com.reservalink.api.application.service.notification.NotificationService;
import com.reservalink.api.adapter.output.repository.BookingRepository;
import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.domain.BookingStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class BookingJob {

    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    public BookingJob(BookingRepository bookingRepository, NotificationService notificationService) {
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 12 * * *")
    @Transactional(rollbackFor = Exception.class)
    public void sendBookingReminders() {
        log.info("Cron job to send booking reminders started");
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime tomorrowEnd = tomorrowStart.plusDays(1).minusNanos(1);

        try {
            List<BookingEntity> bookings =
                    bookingRepository.findBySlotTimeEntityStartDateTimeBetweenAndStatusAndEnabledTrue(
                            tomorrowStart,
                            tomorrowEnd,
                            BookingStatus.CONFIRMED
                    );
            if (!bookings.isEmpty()) {
                log.info("{} bookings found for tomorrow. Sending reminder", bookings.size());
                notificationService.sendBookingReminder(bookings);
            }
            log.info("Cron job finished successfully. {} reminders sent.", bookings.size());
        } catch (Exception e) {
            log.error("Unexpected error sending booking reminders", e);
        }

    }
}
