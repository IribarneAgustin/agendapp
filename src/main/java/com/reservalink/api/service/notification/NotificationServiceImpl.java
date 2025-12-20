package com.reservalink.api.service.notification;

import com.reservalink.api.repository.entity.BookingEntity;
import com.reservalink.api.repository.entity.PaymentStatus;
import com.reservalink.api.repository.entity.SlotTimeEntity;
import com.reservalink.api.repository.entity.SubscriptionPaymentEntity;
import com.reservalink.api.repository.entity.UserEntity;
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
        clientArgs.put("quantity", bookingEntity.getQuantity().toString());
        clientArgs.put("phoneNumber", userEntity.getPhone() != null ? userEntity.getPhone() : "");
        clientArgs.put("cancelLink", baseURL + "/booking/" + bookingEntity.getId() + "/cancel");

        notificationStrategy.send(bookingEntity.getEmail(), NotificationMotive.BOOKING_CONFIRMED, clientArgs);

        Map<String, String> adminArgs = new HashMap<>();
        adminArgs.put("clientName", bookingEntity.getName());
        adminArgs.put("clientEmail", bookingEntity.getEmail());
        adminArgs.put("clientPhoneNumber", bookingEntity.getPhoneNumber());
        adminArgs.put("serviceName", serviceName);
        adminArgs.put("date", dateFormatted);
        adminArgs.put("time", timeFormatted);
        adminArgs.put("quantity", bookingEntity.getQuantity().toString());

        notificationStrategy.send(userEntity.getEmail(), NotificationMotive.ADMIN_BOOKING_CONFIRMED, adminArgs);
    }

    public void sendSubscriptionExpired(List<UserEntity> userEntityList) {
        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationType.EMAIL);
        userEntityList.forEach(user -> {
            try {
                strategy.send(user.getEmail(), NotificationMotive.SUBSCRIPTION_EXPIRED,
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
        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationType.EMAIL);
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

        strategy.send(bookingEntity.getEmail(), NotificationMotive.BOOKING_CANCELLED, clientArgs);

        Map<String, String> adminArgs = new HashMap<>();
        adminArgs.put("clientName", bookingEntity.getName());
        adminArgs.put("clientEmail", bookingEntity.getEmail());
        adminArgs.put("clientPhoneNumber", bookingEntity.getPhoneNumber());
        adminArgs.put("serviceName", serviceName);
        adminArgs.put("date", dateFormatted);
        adminArgs.put("time", timeFormatted);

        strategy.send(user.getEmail(), NotificationMotive.ADMIN_BOOKING_CANCELLED, adminArgs);
    }

    @Override
    public void sendSubscriptionPayment(SubscriptionPaymentEntity subscriptionPayment, UserEntity user) {
        log.info("Sending subscription payment notification for user {}", user.getEmail());
        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationType.EMAIL);
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

        try {
            strategy.send(user.getEmail(), motive, args);
        } catch (Exception e) {
            log.warn("Failed sending subscription payment email to {}", user.getEmail());
        }
    }

    @Override
    public void sendAboutToExpire(Map<Integer, List<UserEntity>> notificationsMap) {
        log.info("Sending batch notifications for subscriptions about to expire.");
        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationType.EMAIL);

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

                    strategy.send(user.getEmail(), NotificationMotive.SUBSCRIPTION_ABOUT_TO_EXPIRE, args);

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
        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationType.EMAIL);
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

                    strategy.send(user.getEmail(), NotificationMotive.SUBSCRIPTION_EXPIRED_RECOVER, args);

                } catch (Exception e) {
                    log.warn("Subscription Expired Recovery Notification failed for the user {} ({} days after)",
                            user.getEmail(), daysAfter, e);
                }
            });
        });
    }

    @Override
    public void sendBookingReminder(List<BookingEntity> incomingBookingList) {
        log.info("Sending reminders for incoming bookings.");
        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationType.EMAIL);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        incomingBookingList.forEach(booking -> {
            Map<String, String> args = new HashMap<>();
            String serviceName = booking.getSlotTimeEntity().getOfferingEntity().getName();
            String dateFormatted = booking.getSlotTimeEntity().getStartDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            String timeFormatted = booking.getSlotTimeEntity().getStartDateTime().toLocalTime().format(timeFormatter)
                    + " - " +
                    booking.getSlotTimeEntity().getEndDateTime().toLocalTime().format(timeFormatter);
            args.put("name", booking.getName());
            args.put("serviceName", serviceName);
            args.put("date", dateFormatted);
            args.put("time", timeFormatted);
            args.put("cancelLink", baseURL + "/booking/" + booking.getId() + "/cancel");
            try {
                strategy.send(booking.getEmail(), NotificationMotive.BOOKING_REMINDER, args);
            } catch (Exception e) {
                log.warn("Unexpected error sending booking reminder to the client {} for the userId {}", booking.getEmail(), booking.getSlotTimeEntity().getOfferingEntity().getUserEntity().getId());
            }

        });
    }

    @Override
    public void sendResetPasswordRequest(String userEmail, String rawToken) {
        log.info("Sending recover password email to {}", userEmail);
        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationType.EMAIL);
        try {
            Map<String, String> args = new HashMap<>();
            args.put("recoverPasswordLink", baseURL + "/public/change-password.html?token=" + rawToken);
            strategy.send(userEmail, NotificationMotive.RESET_PASSWORD, args);
        } catch (Exception e) {
            log.error("Unexpected error sending recover password email to {}", userEmail, e);
        }
    }

    @Override
    public void sendNewUserRegistered(UserEntity userEntity) {
        log.info("Sending welcome email to new user {}", userEntity.getEmail());

        NotificationStrategy strategy = notificationStrategyResolver.resolve(NotificationType.EMAIL);

        try {
            Map<String, String> args = new HashMap<>();
            args.put("userName", userEntity.getName());
            args.put("trialDaysCount", GenericAppConstants.FREE_TIER_DAYS.toString());
            args.put("loginLink", baseURL);

            strategy.send(userEntity.getEmail(), NotificationMotive.NEW_USER_REGISTERED, args);

            //FIXME
            strategy.send("agusiri96@gmail.com", NotificationMotive.APP_NEW_USER_REGISTERED, args);

        } catch (Exception e) {
            log.error("Unexpected error sending welcome new user email to {}", userEntity.getEmail(), e);
        }
    }


}
