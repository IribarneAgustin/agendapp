package com.reservalink.api.application.service.booking;

import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.application.output.BookingRepositoryPort;
import com.reservalink.api.application.service.notification.NotificationChannel;
import com.reservalink.api.application.service.notification.NotificationService;
import com.reservalink.api.application.service.feature.FeatureLifecycleService;
import com.reservalink.api.domain.BookingStatus;
import com.reservalink.api.domain.FeatureName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingReminderServiceImpl implements BookingReminderService {

    private final NotificationService notificationService;
    private final FeatureLifecycleService featureLifecycleService;
    private final BookingRepositoryPort bookingRepositoryPort;

    public BookingReminderServiceImpl(NotificationService notificationService, FeatureLifecycleService featureLifecycleService, BookingRepositoryPort bookingRepositoryPort) {
        this.notificationService = notificationService;
        this.featureLifecycleService = featureLifecycleService;
        this.bookingRepositoryPort = bookingRepositoryPort;
    }

    @Override
    public void sendReminders() {
        log.info("Sending booking reminders for tomorrow.");
        LocalDateTime tomorrowStart = LocalDate.now().plusDays(1).atStartOfDay();
        LocalDateTime tomorrowEnd = tomorrowStart.plusDays(1).minusNanos(1);

         List<BookingEntity> bookings = bookingRepositoryPort.findAllByStatusAndBetweenStartAndEndDateTime(BookingStatus.CONFIRMED, tomorrowStart, tomorrowEnd);

        if (bookings.isEmpty()) {
            log.info("No bookings found for tomorrow.");
            return;
        }

        notificationService.sendBookingReminder(bookings, NotificationChannel.EMAIL);

        Map<String, List<BookingEntity>> bookingsBySubscription =
                bookings.stream()
                        .collect(Collectors.groupingBy(
                                booking -> booking
                                        .getSlotTimeEntity()
                                        .getOfferingEntity()
                                        .getUserEntity()
                                        .getSubscriptionEntity()
                                        .getId()
                        ));

        for (Map.Entry<String, List<BookingEntity>> entry : bookingsBySubscription.entrySet()) {
            String subscriptionId = entry.getKey();
            List<BookingEntity> subscriptionBookings = entry.getValue();

            for (BookingEntity booking : subscriptionBookings) {
                if (featureLifecycleService.canUse(subscriptionId, FeatureName.WHATSAPP_NOTIFICATIONS)) {
                    notificationService.sendBookingReminder(List.of(booking), NotificationChannel.WHATSAPP);
                    featureLifecycleService.consume(subscriptionId, FeatureName.WHATSAPP_NOTIFICATIONS);
                }
            }
        }

        log.info("{} booking reminders processed.", bookings.size());
    }
}
