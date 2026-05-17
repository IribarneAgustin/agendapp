package com.reservalink.api.application.service.payment.strategy;

import com.reservalink.api.application.dto.PaymentDetails;
import com.reservalink.api.application.output.PaymentRepositoryPort;
import com.reservalink.api.application.output.SubscriptionPlanRepositoryPort;
import com.reservalink.api.application.output.SubscriptionRepositoryPort;
import com.reservalink.api.application.output.UserRepositoryPort;
import com.reservalink.api.application.service.feature.FeatureLifecycleService;
import com.reservalink.api.application.service.notification.NotificationService;
import com.reservalink.api.application.service.payment.CheckoutService;
import com.reservalink.api.application.service.subscription.SubscriptionUsageService;
import com.reservalink.api.domain.Subscription;
import com.reservalink.api.domain.SubscriptionPayment;
import com.reservalink.api.domain.SubscriptionPlan;
import com.reservalink.api.domain.User;
import com.reservalink.api.domain.enums.PaymentStatus;
import com.reservalink.api.domain.enums.PaymentType;
import com.reservalink.api.domain.enums.SubscriptionPlanCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SubscriptionPaymentStrategy implements PaymentProcessor {

    private final UserRepositoryPort userRepositoryPort;
    private final FeatureLifecycleService featureLifecycleService;
    private final SubscriptionUsageService subscriptionPlanUsageService;
    private final PaymentRepositoryPort paymentRepositoryPort;
    private final NotificationService notificationService;
    private final CheckoutService checkoutService;
    private final SubscriptionPlanRepositoryPort subscriptionPlanRepositoryPort;
    private final SubscriptionRepositoryPort subscriptionRepositoryPort;

    @Override
    public PaymentType getType() {
        return PaymentType.SUBSCRIPTION;
    }

    @Override
    public void process(PaymentDetails details) {
        String externalId = details.getExternalId();
        SubscriptionPayment subscriptionPayment = paymentRepositoryPort.findSubscriptionPaymentByExternalId(externalId)
                .orElseThrow(() -> new RuntimeException("Subscription Payment not found"));

        Subscription subscription = subscriptionRepositoryPort.findById(subscriptionPayment.getSubscriptionId())
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: " + subscriptionPayment.getSubscriptionId()));

        User user = userRepositoryPort.findBySubscriptionId(subscriptionPayment.getSubscriptionId())
                .orElseThrow(() -> new RuntimeException("User not found with subscription id: " + subscriptionPayment.getSubscriptionId()));

        if (details.isApproved()) {
            if (subscription.getExpiration().isBefore(LocalDateTime.now())) {
                subscription.setExpiration(LocalDateTime.now().plusMonths(1));
            } else {
                subscription.setExpiration(subscription.getExpiration().plusMonths(1));
            }
            subscription.setExpired(false);

            List<String> premiumFeatureIds = extractPremiumFeatureIds(details.getMetadata());
            SubscriptionPlanCode paidPlanCode = extractSubscriptionPlanCode(details.getMetadata());
            Integer selectedResources = extractSelectedResourcesLimit(details.getMetadata());
            SubscriptionPlan currentPlan = subscriptionPlanRepositoryPort.findByUserId(UUID.fromString(user.getId()))
                    .orElseThrow(() -> new EntityNotFoundException("Subscription plan not found"));

            if (currentPlan.getCode() != paidPlanCode || (paidPlanCode == SubscriptionPlanCode.PROFESSIONAL
                    && !subscription.getSelectedResourcesLimit().equals(selectedResources))) {
                SubscriptionPlan newPlan = subscriptionPlanRepositoryPort.findByCode(paidPlanCode)
                        .orElseThrow(() -> new EntityNotFoundException("Subscription plan not found"));
                subscription.setSubscriptionPlanId(newPlan.getId());

                if (SubscriptionPlanCode.BASIC == paidPlanCode) {
                    subscription.setSelectedResourcesLimit(newPlan.getMaxResources());
                } else {
                    subscription.setSelectedResourcesLimit(selectedResources);
                }
            }
            if (!premiumFeatureIds.isEmpty()) {
                featureLifecycleService.renew(premiumFeatureIds, subscription.getId());
            }
            String updatedCheckoutURL = checkoutService.createSubscriptionCheckoutUrl(user.getId(), paidPlanCode, subscription.getSelectedResourcesLimit());
            subscription.setCheckoutLink(updatedCheckoutURL);

            subscriptionRepositoryPort.update(subscription);
            subscriptionPlanUsageService.renew(user.getId(), subscription.getId());
        }

        subscriptionPayment.setPaymentStatus(details.isApproved() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED);
        subscriptionPayment.setPaymentDate(LocalDateTime.now());
        subscriptionPayment.setPaymentMethod(details.getPaymentMethod());

        paymentRepositoryPort.save(subscriptionPayment);
        notificationService.sendSubscriptionPayment(subscriptionPayment, user, subscription.getCheckoutLink());
    }

    private List<String> extractPremiumFeatureIds(Map<String, Object> paymentMetadata) {
        return Optional.ofNullable(paymentMetadata.get("premium_features"))
                .filter(List.class::isInstance)
                .map(List.class::cast)
                .map(list -> list.stream().map(String::valueOf).toList())
                .orElse(Collections.emptyList());
    }

    private SubscriptionPlanCode extractSubscriptionPlanCode(Map<String, Object> metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("Payment metadata is null");
        }

        String planCodeString = (String) metadata.get("subscription_plan");

        if (planCodeString == null) {
            throw new IllegalArgumentException("Subscription Plan Code not found in metadata");
        }

        return SubscriptionPlanCode.valueOf(planCodeString);
    }

    private Integer extractSelectedResourcesLimit(Map<String, Object> metadata) {
        if (metadata == null) {
            throw new IllegalArgumentException("Payment metadata is null");
        }

        Integer selectedResourcesLimit = (Integer) metadata.get("selected_resources_limit");

        if (selectedResourcesLimit == null) {
            throw new IllegalArgumentException("selected_resources_limit not found in metadata");
        }

        return selectedResourcesLimit;
    }
}