package com.agendapp.api.service.notification;

import com.agendapp.api.repository.entity.BookingEntity;
import com.agendapp.api.repository.entity.SlotTimeEntity;
import com.agendapp.api.repository.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationStrategyResolver notificationStrategyResolver;

    @Value("${api.base.url}")
    private String baseURL;

    public NotificationServiceImpl(NotificationStrategyResolver notificationStrategyResolver) {
        this.notificationStrategyResolver = notificationStrategyResolver;
    }

    public void sendBookingConfirmed(BookingEntity bookingEntity) {
        log.info("Sending email notifications for new booking");
        SlotTimeEntity slotTimeEntity = bookingEntity.getSlotTimeEntity();

        UserEntity userEntity = slotTimeEntity.getOfferingEntity().getUserEntity();
        NotificationStrategy notificationStrategy = notificationStrategyResolver.resolve(NotificationType.EMAIL);

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        String dateFormatted = slotTimeEntity.getStartDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String timeFormatted = slotTimeEntity.getStartDateTime().toLocalTime().format(timeFormatter)
                + " - " +
                slotTimeEntity.getEndDateTime().toLocalTime().format(timeFormatter);

        String serviceName = slotTimeEntity.getOfferingEntity().getName();

        Map<String, String> clientArgs = new HashMap<>();
        clientArgs.put("clientName", bookingEntity.getName());
        clientArgs.put("professionalName", userEntity.getName());
        clientArgs.put("serviceName", serviceName);
        clientArgs.put("date", dateFormatted);
        clientArgs.put("time", timeFormatted);
        clientArgs.put("phoneNumber", userEntity.getPhone() != null ? userEntity.getPhone() : "");
        clientArgs.put("cancelLink", baseURL + "/booking/" + bookingEntity.getId() + "/cancel/");

        notificationStrategy.send(bookingEntity.getEmail(), NotificationMotive.BOOKING_CONFIRMED, clientArgs);

        Map<String, String> adminArgs = new HashMap<>();
        adminArgs.put("clientName", bookingEntity.getName());
        adminArgs.put("clientEmail", bookingEntity.getEmail());
        adminArgs.put("clientPhoneNumber", bookingEntity.getPhoneNumber());
        adminArgs.put("serviceName", serviceName);
        adminArgs.put("date", dateFormatted);
        adminArgs.put("time", timeFormatted);

        notificationStrategy.send(userEntity.getEmail(), NotificationMotive.ADMIN_BOOKING_CONFIRMED, adminArgs);
        log.info("Notifications sent successfully");
    }

    public void sendSubscriptionExpired(List<UserEntity> userEntityList) {
        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationType.EMAIL);
        userEntityList.forEach(user -> {
            try {
                strategy.send(user.getEmail(), NotificationMotive.SUBSCRIPTION_EXPIRED,
                        Map.of(
                                "name", user.getName(),
                                "recoverSubscriptionLink", baseURL + "/subscription/" + user.getId() + "/recover",
                                "currentMonth", LocalDate.now().getMonth()
                                        .getDisplayName(TextStyle.FULL, new Locale("es", "ES"))
                                        .toUpperCase(Locale.ROOT)
                        )

                );
            } catch (Exception e) {
                log.warn("Subscription Expired Notification failed for the user {}", user.getEmail());
            }
        });
    }
}
