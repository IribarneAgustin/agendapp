package com.reservalink.api.application.service.booking;

import com.reservalink.api.domain.Booking;

public interface BookingReminderService {
    void sendReminders();

    void scheduleReminder(Booking booking);

    void cancelReminders(String bookingId);
}