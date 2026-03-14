package com.reservalink.api.adapter.input.scheduler;

import com.reservalink.api.application.service.booking.BookingReminderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BookingJob {

    private final BookingReminderService bookingReminderService;

    public BookingJob(BookingReminderService bookingReminderService) {
        this.bookingReminderService = bookingReminderService;
    }

    @Scheduled(cron = "0 0/15 * * * *")
    public void sendBookingReminders() {
        bookingReminderService.sendReminders();
    }
}
