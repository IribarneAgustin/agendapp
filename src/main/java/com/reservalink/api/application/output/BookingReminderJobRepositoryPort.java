package com.reservalink.api.application.output;

import com.reservalink.api.domain.BookingReminderJob;
import com.reservalink.api.domain.enums.BookingReminderJobStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingReminderJobRepositoryPort {

    BookingReminderJob save(BookingReminderJob job);

    List<BookingReminderJob> findEligibleReminders(LocalDateTime triggerTime);

    void updateStatus(String jobId, BookingReminderJobStatus status, LocalDateTime processedAt);

    void updateRetry(String jobId);

    List<BookingReminderJob> findByBookingIdAndStatusIn(String bookingId, List<BookingReminderJobStatus> statusList);
}