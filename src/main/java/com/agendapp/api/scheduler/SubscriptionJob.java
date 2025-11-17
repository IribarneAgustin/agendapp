package com.agendapp.api.scheduler;

import com.agendapp.api.repository.entity.SubscriptionEntity;
import com.agendapp.api.repository.entity.UserEntity;
import com.agendapp.api.repository.BookingRepository;
import com.agendapp.api.repository.SubscriptionRepository;
import com.agendapp.api.repository.UserRepository;
import com.agendapp.api.service.notification.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class SubscriptionJob {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final NotificationService notificationService;

    public SubscriptionJob(SubscriptionRepository subscriptionRepository, UserRepository userRepository, BookingRepository bookingRepository, NotificationService notificationService) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 0 22 * * *") // daily at 10 pm
    @Transactional(rollbackFor = Exception.class)
    public void checkForDueSubscriptions() {
        log.info("STARTING EXPIRED SUBSCRIPTIONS BATCH PROCESS");
        List<SubscriptionEntity> expiredSubscriptionEntities =
                subscriptionRepository.findByEnabledTrueAndExpiredFalseAndExpirationLessThan(LocalDateTime.now());

        if(expiredSubscriptionEntities.isEmpty()) {
            log.info("No subscriptions expired found. Cron job skipped");
            return;
        }
        log.info("{} subscriptions expired found", expiredSubscriptionEntities.size());
        expiredSubscriptionEntities.forEach(subscription -> subscription.setExpired(Boolean.TRUE));
        subscriptionRepository.saveAllAndFlush(expiredSubscriptionEntities);

        try {
            List<String> subscriptionIds = expiredSubscriptionEntities.stream().map(SubscriptionEntity::getId).toList();
            List<UserEntity> userEntityList = userRepository.findAllBySubscriptionEntityIdIn(subscriptionIds);
            notificationService.sendSubscriptionExpired(userEntityList);
        } catch (Exception e) {
            log.warn("Unexpected error sending subscription expired notifications");
        }

        log.info("FINISHED EXPIRED SUBSCRIPTIONS BATCH PROCESS SUCCESSFULLY");
    }


}
