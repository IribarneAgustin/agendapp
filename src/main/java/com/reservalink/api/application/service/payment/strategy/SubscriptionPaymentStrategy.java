package com.reservalink.api.application.service.payment.strategy;

import com.reservalink.api.application.dto.PaymentDetails;
import com.reservalink.api.application.output.PaymentRepositoryPort;
import com.reservalink.api.application.output.SubscriptionPlanRepositoryPort;
import com.reservalink.api.application.output.UserRepositoryPort;
import com.reservalink.api.application.service.feature.FeatureLifecycleService;
import com.reservalink.api.application.service.notification.NotificationService;
import com.reservalink.api.application.service.payment.CheckoutService;
import com.reservalink.api.application.service.subscription.SubscriptionUsageService;
import com.reservalink.api.domain.Subscription;
import com.reservalink.api.domain.SubscriptionPayment;
import com.reservalink.api.domain.SubscriptionPlan;
import com.reservalink.api.domain.User;
import com.reservalink.api.domain.enums.PaymentMethod;
import com.reservalink.api.domain.enums.PaymentStatus;
import com.reservalink.api.domain.enums.PaymentType;
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

    @Override
    public PaymentType getType() {
        return PaymentType.SUBSCRIPTION;
    }

    @Override
    public void process(PaymentDetails details) {
        String externalId = details.getExternalId();
        String userId = externalId.replace("SUBSCRIPTION-", "");

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + userId));

        Subscription subscription = userRepositoryPort.findUserSubscriptionByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found with userId: " + userId));

        if (details.isApproved()) {
            if (subscription.getExpiration().isBefore(LocalDateTime.now())) {
                subscription.setExpiration(LocalDateTime.now().plusMonths(1));
            } else {
                subscription.setExpiration(subscription.getExpiration().plusMonths(1));
            }
            subscription.setExpired(false);

            List<String> premiumFeatureIds = extractPremiumFeatureIds(details.getMetadata());
            if (!premiumFeatureIds.isEmpty()) {
                featureLifecycleService.renew(premiumFeatureIds, subscription.getId());
                SubscriptionPlan currentPlan = subscriptionPlanRepositoryPort.findByUserId(UUID.fromString(userId))
                        .orElseThrow(() -> new EntityNotFoundException("Subscription plan not found"));
                String updatedCheckoutURL = checkoutService.createSubscriptionCheckoutUrl(userId, currentPlan.getCode(), subscription.getSelectedResourcesLimit());
                subscription.setCheckoutLink(updatedCheckoutURL);
            }
            subscriptionPlanUsageService.renew(userId, subscription.getId());
        }
        SubscriptionPayment subscriptionPayment = SubscriptionPayment.builder()
                .subscriptionId(user.getSubscriptionId())
                .enabled(true)
                .externalId(externalId)
                .paymentStatus(details.isApproved() ? PaymentStatus.COMPLETED : PaymentStatus.FAILED)
                .paymentMethod(PaymentMethod.MERCADO_PAGO)
                .amount(details.getAmount())
                .paymentDate(LocalDateTime.now())
                .build();
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
}