package com.reservalink.api.scheduler;

import com.reservalink.api.repository.BookingRepository;
import com.reservalink.api.repository.entity.BookingEntity;
import com.reservalink.api.repository.entity.BookingStatus;
import com.reservalink.api.service.notification.NotificationService;
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
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime tomorrowEnd   = tomorrowStart.plusDays(1).minusNanos(1);

        try {
            List<BookingEntity> bookings =
                    bookingRepository.findBySlotTimeEntityStartDateTimeBetweenAndStatusAndEnabledTrue(
                            tomorrowStart,
                            tomorrowEnd,
                            BookingStatus.CONFIRMED.name()
                    );
            notificationService.sendBookingReminder(bookings);
        } catch (Exception e) {
            log.error("Unexpected error sending booking reminders", e);
        }

    }
}
