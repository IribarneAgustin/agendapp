package com.reservalink.api.application.service.subscription;

import com.reservalink.api.application.dto.SubscriptionPlanBilling;
import com.reservalink.api.application.dto.SubscriptionStatusResponse;
import com.reservalink.api.application.dto.SubscriptionUsageResponse;
import com.reservalink.api.application.output.ResourceRepositoryPort;
import com.reservalink.api.application.output.SubscriptionPlanRepositoryPort;
import com.reservalink.api.application.output.SubscriptionUsageRepositoryPort;
import com.reservalink.api.application.output.UserRepositoryPort;
import com.reservalink.api.domain.Subscription;
import com.reservalink.api.domain.SubscriptionPlan;
import com.reservalink.api.domain.SubscriptionUsage;
import com.reservalink.api.domain.enums.FeatureName;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanServiceImpl implements SubscriptionPlanService {

    private final SubscriptionPlanRepositoryPort subscriptionPlanRepositoryPort;
    private final SubscriptionUsageRepositoryPort subscriptionUsageRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final ResourceRepositoryPort resourceRepositoryPort;

    @Override
    public SubscriptionStatusResponse findSubscriptionStatus(UUID userId) {
        SubscriptionPlan plan = subscriptionPlanRepositoryPort.findByUserId(userId)
                .orElseThrow(EntityNotFoundException::new);

        Subscription subscription = userRepositoryPort.findUserSubscriptionByUserId(userId.toString())
                .orElseThrow(EntityNotFoundException::new);

        SubscriptionUsage usage = subscriptionUsageRepositoryPort.findCurrentBySubscriptionId(subscription.getId())
                .orElseGet(() -> SubscriptionUsage.builder()
                        .subscriptionId(subscription.getId())
                        .bookingUsage(0)
                        .build()
                );

        SubscriptionPlanBilling billing = SubscriptionPlanBilling.builder()
                .startDate(usage.getStartPeriodDateTime() != null ? usage.getStartPeriodDateTime() : subscription.getCreationDateTime())
                .endDate(subscription.getExpiration())
                .build();

        SubscriptionUsageResponse bookings = SubscriptionUsageResponse.builder()
                .featureName(FeatureName.BOOKINGS.name())
                .used(usage.getBookingUsage())
                .limit(plan.getMaxBookings())
                .build();

        Integer resourceLimit = plan.getMaxResources() != null ? plan.getMaxResources() : subscription.getSelectedResourcesLimit();
        int resourceUsage = resourceRepositoryPort.findAllByUserId(userId.toString()).size();

        SubscriptionUsageResponse resources = SubscriptionUsageResponse.builder()
                .featureName(FeatureName.RESOURCES.name())
                .used(resourceUsage)
                .limit(resourceLimit)
                .build();

        return SubscriptionStatusResponse.builder()
                .planName(plan.getCode().name())
                .features(List.of(bookings, resources))
                .billing(billing)
                .checkoutLink(subscription.getCheckoutLink())
                .build();
    }

        /*

        Subscription subscription = userRepositoryPort.findUserSubscriptionByUserId(userId.toString())
        .orElseThrow(() -> new EntityNotFoundException("Subscription not found"));

        if (SubscriptionPlanCode.PROFESSIONAL == planCode) {
            subscription.setSelectedResourcesLimit(selectedResources);
        } else {
            subscription.setSelectedResourcesLimit(targetPlan.getMaxResources());
        }
        subscription.setSubscriptionPlanId(targetPlan.getId());

        String externalId = String.format("%s-%s", PaymentType.SUBSCRIPTION.name(), userId);
        SubscriptionPaymentMetadata metadata = new SubscriptionPaymentMetadata(userId.toString(), planCode, subscription.getSelectedResourcesLimit());

        String checkoutUrl = paymentGatewayPort.generateCheckoutUrl(
                "ReservaLink - Suscripción " + planCode.name(),
                finalPrice,
                externalId,
                metadata
        );

        subscription.setCheckoutLink(checkoutUrl);

        subscriptionPlanUsageService.renew(userId.toString(), subscription.getId());
        subscriptionRepositoryPort.update(subscription);
    }*/
}