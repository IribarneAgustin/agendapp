package com.reservalink.api.application.service.notification;

import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.adapter.output.repository.entity.SlotTimeEntity;
import com.reservalink.api.adapter.output.repository.entity.SubscriptionPaymentEntity;
import com.reservalink.api.adapter.output.repository.entity.UserEntity;
import com.reservalink.api.application.service.notification.strategy.NotificationStrategy;
import com.reservalink.api.application.service.notification.strategy.NotificationStrategyResolver;
import com.reservalink.api.domain.PaymentStatus;
import com.reservalink.api.utils.GenericAppConstants;
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

    @Override
    public void sendBookingConfirmed(BookingEntity bookingEntity) {
        log.info("Sending email notifications for new booking");
        SlotTimeEntity slotTimeEntity = bookingEntity.getSlotTimeEntity();

        UserEntity userEntity = slotTimeEntity.getOfferingEntity().getUserEntity();
        NotificationStrategy notificationStrategy = notificationStrategyResolver.resolve(NotificationChannel.EMAIL);

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
        clientArgs.put("quantity", bookingEntity.getQuantity().toString());
        clientArgs.put("phoneNumber", userEntity.getPhone() != null ? userEntity.getPhone() : "");
        clientArgs.put("cancelLink", baseURL + "/booking/" + bookingEntity.getId() + "/cancel");

        NotificationTarget target = NotificationTarget.builder()
                .email(bookingEntity.getEmail())
                .phone(bookingEntity.getPhoneNumber())
                .build();

        notificationStrategy.send(target, NotificationMotive.BOOKING_CONFIRMED, clientArgs);

        Map<String, String> adminArgs = new HashMap<>();
        adminArgs.put("clientName", bookingEntity.getName());
        adminArgs.put("clientEmail", bookingEntity.getEmail());
        adminArgs.put("clientPhoneNumber", bookingEntity.getPhoneNumber());
        adminArgs.put("serviceName", serviceName);
        adminArgs.put("date", dateFormatted);
        adminArgs.put("time", timeFormatted);
        adminArgs.put("quantity", bookingEntity.getQuantity().toString());

        NotificationTarget targetUser = NotificationTarget.builder()
                .email(userEntity.getEmail())
                .build();

        notificationStrategy.send(targetUser, NotificationMotive.ADMIN_BOOKING_CONFIRMED, adminArgs);
    }

    @Override
    public void sendSubscriptionExpired(List<UserEntity> userEntityList) {
        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationChannel.EMAIL);
        userEntityList.forEach(user -> {
            try {
                NotificationTarget target = NotificationTarget.builder()
                        .email(user.getEmail())
                        .build();
                strategy.send(target, NotificationMotive.SUBSCRIPTION_EXPIRED,
                        Map.of(
                                "name", user.getName(),
                                "recoverSubscriptionLink", user.getSubscriptionEntity().getCheckoutLink(),
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

    @Override
    public void sendBookingCancelled(BookingEntity bookingEntity) {
        log.info("Sending email notifications for booking cancelled");
        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationChannel.EMAIL);
        UserEntity user = bookingEntity.getSlotTimeEntity().getOfferingEntity().getUserEntity();
        SlotTimeEntity slotTimeEntity = bookingEntity.getSlotTimeEntity();

        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String serviceName = slotTimeEntity.getOfferingEntity().getName();
        String dateFormatted = slotTimeEntity.getStartDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        String timeFormatted = slotTimeEntity.getStartDateTime().toLocalTime().format(timeFormatter)
                + " - " +
                slotTimeEntity.getEndDateTime().toLocalTime().format(timeFormatter);

        Map<String, String> clientArgs = new HashMap<>();
        clientArgs.put("clientName", bookingEntity.getName());
        clientArgs.put("professionalName", user.getName());
        clientArgs.put("serviceName", serviceName);
        clientArgs.put("date", dateFormatted);
        clientArgs.put("time", timeFormatted);
        clientArgs.put("phoneNumber", user.getPhone() != null ? user.getPhone() : "");

        NotificationTarget target = NotificationTarget.builder()
                .email(bookingEntity.getEmail())
                .phone(bookingEntity.getPhoneNumber())
                .build();

        strategy.send(target, NotificationMotive.BOOKING_CANCELLED, clientArgs);

        Map<String, String> adminArgs = new HashMap<>();
        adminArgs.put("clientName", bookingEntity.getName());
        adminArgs.put("clientEmail", bookingEntity.getEmail());
        adminArgs.put("clientPhoneNumber", bookingEntity.getPhoneNumber());
        adminArgs.put("serviceName", serviceName);
        adminArgs.put("date", dateFormatted);
        adminArgs.put("time", timeFormatted);

        NotificationTarget targetUser = NotificationTarget.builder()
                .email(user.getEmail())
                .build();

        strategy.send(targetUser, NotificationMotive.ADMIN_BOOKING_CANCELLED, adminArgs);
    }

    @Override
    public void sendSubscriptionPayment(SubscriptionPaymentEntity subscriptionPayment, UserEntity user) {
        log.info("Sending subscription payment notification for user {}", user.getEmail());
        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationChannel.EMAIL);
        boolean success = subscriptionPayment.getPaymentStatus().equals(PaymentStatus.COMPLETED);

        Map<String, String> args = new HashMap<>();
        args.put("name", user.getName());
        args.put("amount", subscriptionPayment.getAmount().toPlainString());
        args.put("paymentDate", subscriptionPayment.getPaymentDate().toLocalDate().toString());
        args.put("currentYear", String.valueOf(LocalDate.now().getYear()));
        String month = LocalDate.now()
                .getMonth()
                .getDisplayName(TextStyle.FULL, new Locale("es", "ES"))
                .toUpperCase(Locale.ROOT);
        args.put("month", month);

        if (!success) {
            args.put("recoverSubscriptionLink", user.getSubscriptionEntity().getCheckoutLink());
            args.put("failed", "true");
        } else {
            args.put("success", "true");
        }

        NotificationMotive motive = success ? NotificationMotive.SUBSCRIPTION_PAYMENT_SUCCESS : NotificationMotive.SUBSCRIPTION_PAYMENT_FAILED;
        NotificationTarget target = NotificationTarget.builder()
                .email(user.getEmail())
                .phone(user.getPhone())
                .build();
        try {
            strategy.send(target, motive, args);
        } catch (Exception e) {
            log.warn("Failed sending subscription payment email to {}", user.getEmail());
        }
    }

    @Override
    public void sendAboutToExpire(Map<Integer, List<UserEntity>> notificationsMap) {
        log.info("Sending batch notifications for subscriptions about to expire.");
        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationChannel.EMAIL);

        Locale spanishLocale = new Locale("es", "ES");
        String currentMonthName = LocalDate.now().getMonth()
                .getDisplayName(TextStyle.FULL, spanishLocale)
                .toUpperCase(Locale.ROOT);

        notificationsMap.forEach((daysBefore, userEntityList) -> {

            userEntityList.forEach(user -> {
                try {
                    Map<String, String> args = new HashMap<>();
                    args.put("name", user.getName());
                    args.put("daysBefore", daysBefore.toString());
                    args.put("currentMonth", currentMonthName);
                    args.put("recoverSubscriptionLink", user.getSubscriptionEntity().getCheckoutLink());
                    NotificationTarget target = NotificationTarget.builder()
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            .build();
                    strategy.send(target, NotificationMotive.SUBSCRIPTION_ABOUT_TO_EXPIRE, args);

                } catch (Exception e) {
                    log.warn("Subscription About To Expire Notification failed for the user {} ({} days before)",
                            user.getEmail(), daysBefore, e);
                }
            });
        });
    }

    @Override
    public void sendRecoverExpired(Map<Integer, List<UserEntity>> notificationsMap) {
        log.info("Sending batch notifications for recovering expired subscriptions.");
        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationChannel.EMAIL);
        Locale spanishLocale = new Locale("es", "ES");
        String currentMonthName = LocalDate.now().getMonth()
                .getDisplayName(TextStyle.FULL, spanishLocale)
                .toUpperCase(Locale.ROOT);

        notificationsMap.forEach((daysAfter, userEntityList) -> {

            userEntityList.forEach(user -> {
                try {
                    Map<String, String> args = new HashMap<>();
                    args.put("name", user.getName());
                    args.put("daysAfter", daysAfter.toString());
                    args.put("currentMonth", currentMonthName);
                    args.put("recoverSubscriptionLink", user.getSubscriptionEntity().getCheckoutLink());
                    NotificationTarget target = NotificationTarget.builder()
                            .email(user.getEmail())
                            .phone(user.getPhone())
                            .build();
                    strategy.send(target, NotificationMotive.SUBSCRIPTION_EXPIRED_RECOVER, args);

                } catch (Exception e) {
                    log.warn("Subscription Expired Recovery Notification failed for the user {} ({} days after)",
                            user.getEmail(), daysAfter, e);
                }
            });
        });
    }

    @Override
    public void sendBookingReminder(List<BookingEntity> incomingBookingList, NotificationChannel channel) {
        log.info("Sending {} reminders for incoming bookings.", channel);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        incomingBookingList.forEach(booking -> {

            String subscriptionId = booking
                    .getSlotTimeEntity()
                    .getOfferingEntity()
                    .getUserEntity()
                    .getSubscriptionEntity()
                    .getId();

            Map<String, String> args = new HashMap<>();
            args.put("name", booking.getName());
            args.put("serviceName", booking.getSlotTimeEntity().getOfferingEntity().getName());
            args.put("date", booking.getSlotTimeEntity()
                    .getStartDateTime()
                    .format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            args.put("time",
                    booking.getSlotTimeEntity().getStartDateTime().toLocalTime().format(timeFormatter)
                            + " - " +
                            booking.getSlotTimeEntity().getEndDateTime().toLocalTime().format(timeFormatter)
            );
            args.put("cancelLink", baseURL + "/booking/" + booking.getId());
            args.put("subscriptionId", subscriptionId);
            args.put("bookingId", booking.getId());

            NotificationTarget target = NotificationTarget.builder()
                    .email(booking.getEmail())
                    .phone(booking.getPhoneNumber())
                    .build();

            notificationStrategyResolver
                    .resolve(channel)
                    .send(target, NotificationMotive.BOOKING_REMINDER, args);
        });
    }

    @Override
    public void sendResetPasswordRequest(String userEmail, String rawToken) {
        log.info("Sending recover password email to {}", userEmail);
        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationChannel.EMAIL);
        try {
            Map<String, String> args = new HashMap<>();
            args.put("recoverPasswordLink", baseURL + "/public/change-password.html?token=" + rawToken);
            NotificationTarget target = NotificationTarget.builder().email(userEmail).build();
            strategy.send(target, NotificationMotive.RESET_PASSWORD, args);
        } catch (Exception e) {
            log.error("Unexpected error sending recover password email to {}", userEmail, e);
        }
    }

    @Override
    public void sendNewUserRegistered(UserEntity userEntity) {
        log.info("Sending welcome email to new user {}", userEntity.getEmail());

        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationChannel.EMAIL);

        try {
            Map<String, String> args = new HashMap<>();
            args.put("userName", userEntity.getName());
            args.put("trialDaysCount", GenericAppConstants.FREE_TIER_DAYS.toString());
            args.put("loginLink", baseURL);
            NotificationTarget target = NotificationTarget.builder()
                    .email(userEntity.getEmail())
                    .phone(userEntity.getPhone())
                    .build();
            strategy.send(target, NotificationMotive.NEW_USER_REGISTERED, args);

            NotificationTarget targetApp = NotificationTarget.builder()
                    .email("agusiri96@gmail.com")
                    .build();
            strategy.send(targetApp, NotificationMotive.APP_NEW_USER_REGISTERED, args);

        } catch (Exception e) {
            log.error("Unexpected error sending welcome new user email to {}", userEntity.getEmail(), e);
        }
    }

}
