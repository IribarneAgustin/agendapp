package com.reservalink.api.scheduler;

import com.reservalink.api.repository.PaymentRepository;
import com.reservalink.api.repository.entity.SubscriptionEntity;
import com.reservalink.api.repository.entity.SubscriptionNotificationEntity;
import com.reservalink.api.repository.entity.UserEntity;
import com.reservalink.api.repository.BookingRepository;
import com.reservalink.api.repository.SubscriptionRepository;
import com.reservalink.api.repository.UserRepository;
import com.reservalink.api.service.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class SubscriptionJob {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public SubscriptionJob(SubscriptionRepository subscriptionRepository, UserRepository userRepository, BookingRepository bookingRepository, NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 20 * * *")
    @Transactional(rollbackFor = Exception.class)
    public void checkForDueSubscriptions() {
        log.info("STARTING EXPIRED SUBSCRIPTIONS BATCH PROCESS");
        List<UserEntity> dueUserEntities = userRepository
                .findBySubscriptionEntity_EnabledTrueAndSubscriptionEntity_ExpiredFalseAndSubscriptionEntity_ExpirationBefore(
                        LocalDateTime.now());

        if (dueUserEntities.isEmpty()) {
            log.info("No subscriptions expired found. Cron job skipped");
            return;
        }

        log.info("{} subscriptions expired found and will be marked as EXPIRED.", dueUserEntities.size());
        List<SubscriptionEntity> subscriptionsToUpdate = dueUserEntities.stream()
                .map(UserEntity::getSubscriptionEntity)
                .peek(subscription -> subscription.setExpired(Boolean.TRUE))
                .toList();

        subscriptionRepository.saveAllAndFlush(subscriptionsToUpdate);

        try {
            notificationService.sendSubscriptionExpired(dueUserEntities);
        } catch (Exception e) {
            log.error("Unexpected error sending subscription expired notifications", e);
        }

        log.info("FINISHED EXPIRED SUBSCRIPTIONS BATCH PROCESS");
    }

    @Scheduled(cron = "0 46 1 * * *")
    @Transactional(rollbackFor = Exception.class)
    public void sendExpirationNotifications() {
        log.info("Running cron job to send subscription expired notifications");
        final LocalDate today = LocalDate.now();

        LocalDateTime startOf3DaysFuture = today.plusDays(3).atStartOfDay();
        LocalDateTime endOf3DaysFuture = LocalDateTime.of(today.plusDays(3), LocalTime.MAX);

        LocalDateTime startOfTomorrow = today.plusDays(1).atStartOfDay();
        LocalDateTime endOfTomorrow = LocalDateTime.of(today.plusDays(1), LocalTime.MAX);

        LocalDateTime startOf3DaysAgo = today.minusDays(3).atStartOfDay();
        LocalDateTime endOf3DaysAgo = LocalDateTime.of(today.minusDays(3), LocalTime.MAX);

        LocalDateTime startOf5DaysAgo = today.minusDays(5).atStartOfDay();
        LocalDateTime endOf5DaysAgo = LocalDateTime.of(today.minusDays(5), LocalTime.MAX);

        try {
            List<UserEntity> expiringIn3Days = userRepository
                    .findBySubscriptionEntity_EnabledTrueAndSubscriptionEntity_ExpiredFalseAndSubscriptionEntity_ExpirationBetween(
                            startOf3DaysFuture, endOf3DaysFuture);

            List<UserEntity> expiringTomorrow = userRepository
                    .findBySubscriptionEntity_EnabledTrueAndSubscriptionEntity_ExpiredFalseAndSubscriptionEntity_ExpirationBetween(
                            startOfTomorrow, endOfTomorrow);

            List<UserEntity> expired3DaysAgo = userRepository
                    .findBySubscriptionEntity_EnabledTrueAndSubscriptionEntity_ExpiredTrueAndSubscriptionEntity_ExpirationBetween(
                            startOf3DaysAgo, endOf3DaysAgo);

            List<UserEntity> expired5DaysAgo = userRepository
                    .findBySubscriptionEntity_EnabledTrueAndSubscriptionEntity_ExpiredTrueAndSubscriptionEntity_ExpirationBetween(
                            startOf5DaysAgo, endOf5DaysAgo);


            Map<Integer, List<UserEntity>> subscriptionAboutToExpireMap =
                    Stream.of(
                                    Map.entry(3, expiringIn3Days),
                                    Map.entry(1, expiringTomorrow)
                            )
                            .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            Map<Integer, List<UserEntity>> subscriptionExpiredMap =
                    Stream.of(
                                    Map.entry(3, expired3DaysAgo),
                                    Map.entry(5, expired5DaysAgo)
                            )
                            .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


            if (!subscriptionAboutToExpireMap.isEmpty()) {
                notificationService.sendAboutToExpire(subscriptionAboutToExpireMap);
            }

            if (!subscriptionExpiredMap.isEmpty()) {
                notificationService.sendRecoverExpired(subscriptionExpiredMap);
            }
            log.info("Cron job ran successfully");
        } catch (Exception e) {
            log.error("Unexpected error sending notifications to subscriptions about to expire and to recover expired ones", e);
        }

    }


}
