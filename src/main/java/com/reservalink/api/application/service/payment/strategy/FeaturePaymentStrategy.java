package com.reservalink.api.application.service.payment.strategy;

import com.reservalink.api.application.dto.PaymentDetails;
import com.reservalink.api.application.output.FeatureUsageRepositoryPort;
import com.reservalink.api.application.output.SubscriptionPlanRepositoryPort;
import com.reservalink.api.application.output.SubscriptionRepositoryPort;
import com.reservalink.api.application.output.UserRepositoryPort;
import com.reservalink.api.application.service.payment.CheckoutService;
import com.reservalink.api.domain.FeatureUsage;
import com.reservalink.api.domain.Subscription;
import com.reservalink.api.domain.SubscriptionPlan;
import com.reservalink.api.domain.User;
import com.reservalink.api.domain.enums.FeatureName;
import com.reservalink.api.domain.enums.FeatureStatus;
import com.reservalink.api.domain.enums.PaymentType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class FeaturePaymentStrategy implements PaymentProcessor {

    private final FeatureUsageRepositoryPort featureUsageRepositoryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final CheckoutService checkoutService;
    private final SubscriptionPlanRepositoryPort subscriptionPlanRepositoryPort;

    @Override
    public PaymentType getType() {
        return PaymentType.FEATURE;
    }

    @Override
    public void process(PaymentDetails details) {
        String externalId = details.getExternalId();
        String featureUsageId = externalId.replace("FEATURE-", "");
        if (details.isApproved()) {
            log.info("Processing payment for FeatureUsage id {}", featureUsageId);
            FeatureUsage featureUsage = featureUsageRepositoryPort.findById(featureUsageId)
                    .orElseThrow(() -> new IllegalArgumentException("Feature Usage Id not found: " + featureUsageId));

            //TODO: when more features premium be added, refactor to handle the name correctly. (fetch from their repo)
            featureUsageRepositoryPort.findByUserSubscriptionIdAndFeatureNameAndStatus(featureUsage.getSubscriptionId(), FeatureName.WHATSAPP_NOTIFICATIONS, FeatureStatus.ACTIVE)
                    .ifPresent(activeFeature -> {
                        activeFeature.setFeatureStatus(FeatureStatus.EXCHANGED);
                        featureUsageRepositoryPort.update(activeFeature);
                    });

            featureUsage.setFeatureStatus(FeatureStatus.ACTIVE);
            featureUsage.setFirstCycle(true);
            featureUsage.setActivatedAt(LocalDateTime.now());
            featureUsageRepositoryPort.update(featureUsage);

            String userSubscriptionId = featureUsage.getSubscriptionId();
            Subscription userSubscription = subscriptionRepositoryPort.findById(userSubscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("Subscription Id not found: " + userSubscriptionId));

            User user = userRepositoryPort.findBySubscriptionId(userSubscriptionId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found for subscription id: " + userSubscriptionId));

            SubscriptionPlan currentPlan = subscriptionPlanRepositoryPort.findByUserId(UUID.fromString(user.getId()))
                    .orElseThrow(() -> new EntityNotFoundException("Subscription plan not found"));

            String newSubscriptionCheckoutURL = checkoutService.createSubscriptionCheckoutUrl(user.getId(), currentPlan.getCode(), userSubscription.getSelectedResourcesLimit());
            userSubscription.setCheckoutLink(newSubscriptionCheckoutURL);

            subscriptionRepositoryPort.update(userSubscription);
            log.info("Payment processed successfully");
        } else {
            log.info("Feature payment failed for Feature Usage id {}", featureUsageId);
        }
    }
}
