package com.agendapp.api.scheduler;

import com.agendapp.api.entity.Subscription;
import com.agendapp.api.entity.User;
import com.agendapp.api.repository.SubscriptionRepository;
import com.agendapp.api.repository.UserRepository;
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

    public SubscriptionJob(SubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 22 * * *") // daily at 10 pm
    @Transactional(rollbackFor = Exception.class)
    public void checkForDueSubscriptions() {
        log.info("STARTING EXPIRED SUBSCRIPTIONS BATCH PROCESS");
        List<Subscription> expiredSubscriptions =
                subscriptionRepository.findByEnabledTrueAndExpiredFalseAndExpirationLessThan(LocalDateTime.now());

        if(expiredSubscriptions.isEmpty()) {
            log.info("No subscriptions expired found. Cron job skipped");
            return;
        }
        log.info("{} subscriptions expired found", expiredSubscriptions.size());
        expiredSubscriptions.forEach(subscription -> subscription.setExpired(Boolean.TRUE));
        subscriptionRepository.saveAllAndFlush(expiredSubscriptions);

        /*TODO send notification to users (delegate to notificationService). Also add pre reminders
        List<String> subscriptionIds = expiredSubscriptions.stream().map(Subscription::getId).toList();
        List<User> userList = userRepository.findAllBySubscriptionIdIn(subscriptionIds);
        */
        log.info("FINISHED EXPIRED SUBSCRIPTIONS BATCH PROCESS SUCCESSFULLY");
    }


}
