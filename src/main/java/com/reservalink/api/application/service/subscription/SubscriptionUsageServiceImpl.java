package com.reservalink.api.application.service.subscription;

import com.reservalink.api.application.output.ResourceRepositoryPort;
import com.reservalink.api.application.output.SubscriptionPlanRepositoryPort;
import com.reservalink.api.application.output.SubscriptionUsageRepositoryPort;
import com.reservalink.api.domain.SubscriptionPlan;
import com.reservalink.api.domain.SubscriptionUsage;
import com.reservalink.api.domain.enums.FeatureName;
import com.reservalink.api.domain.enums.PeriodStatus;
import com.reservalink.api.domain.enums.SubscriptionPlanCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionUsageServiceImpl implements SubscriptionUsageService {

    private final SubscriptionUsageRepositoryPort subscriptionUsageRepositoryPort;
    private final SubscriptionPlanRepositoryPort subscriptionPlanRepositoryPort;
    private final ResourceRepositoryPort resourceRepositoryPort;

    @Override
    public void renew(String userId, String subscriptionId) {
        List<SubscriptionUsage> activeUsages = subscriptionUsageRepositoryPort
                .findAllBySubscriptionIdAndPeriodStatus(subscriptionId, PeriodStatus.ACTIVE);

        if (activeUsages.size() > 1) {
            log.error("Multiple ACTIVE usages found for subscription {}", subscriptionId);
        }

        activeUsages.forEach(active -> {
            active.setPeriodStatus(PeriodStatus.CLOSED);
            subscriptionUsageRepositoryPort.save(active);
        });

        SubscriptionUsage newUsage = SubscriptionUsage.builder()
                .subscriptionId(subscriptionId)
                .periodStatus(PeriodStatus.ACTIVE)
                .bookingUsage(0)
                .startPeriodDateTime(LocalDateTime.now())
                .enabled(true)
                .build();

        subscriptionUsageRepositoryPort.save(newUsage);
    }

    @Override
    public boolean canConsume(String subscriptionId, String planId, FeatureName feature) {
        SubscriptionPlan plan = subscriptionPlanRepositoryPort.findById(planId).orElseThrow(EntityNotFoundException::new);
        boolean canConsume = true;

        if (isTrackable(plan.getCode(), feature)) {
            SubscriptionUsage usage = getOrCreateActiveUsage(subscriptionId);

            if (FeatureName.BOOKINGS == feature) {
                Integer limit = plan.getMaxBookings();
                if (limit != null) {
                    canConsume = usage.getBookingUsage() < limit;
                }
            }

            if (FeatureName.RESOURCES == feature) {
                Integer limit = plan.getMaxResources();
                Integer currentResourcesCount = resourceRepositoryPort.findAllBySubscriptionId(subscriptionId).size();
                if (limit != null) {
                    canConsume = currentResourcesCount < limit;
                }
            }
        }
        return canConsume;
    }

    @Override
    public void consume(String subscriptionId, String planId, FeatureName feature) {
        SubscriptionPlan plan = subscriptionPlanRepositoryPort.findById(planId)
                .orElseThrow(EntityNotFoundException::new);

        if (isTrackable(plan.getCode(), feature)) {
            SubscriptionUsage usage = getOrCreateActiveUsage(subscriptionId);
            if (FeatureName.BOOKINGS == feature && plan.getMaxBookings() != null) {
                usage.setBookingUsage(usage.getBookingUsage() + 1);
                subscriptionUsageRepositoryPort.save(usage);
                log.info("Usage incremented for {} in plan {}. New total: {}", feature, plan.getCode(), usage.getBookingUsage());
            }
        }
    }

    private SubscriptionUsage getOrCreateActiveUsage(String subscriptionId) {
        Optional<SubscriptionUsage> usageOpt = subscriptionUsageRepositoryPort
                .findAllBySubscriptionIdAndPeriodStatus(subscriptionId, PeriodStatus.ACTIVE)
                .stream()
                .findFirst();

        SubscriptionUsage usage;
        if (usageOpt.isPresent()) {
            usage = usageOpt.get();
        } else {
            log.warn("SubscriptionUsage not found for subscriptionId: {}, creating a new one.", subscriptionId);
            usage = SubscriptionUsage.builder()
                    .subscriptionId(subscriptionId)
                    .periodStatus(PeriodStatus.ACTIVE)
                    .bookingUsage(0)
                    .startPeriodDateTime(LocalDateTime.now())
                    .enabled(true)
                    .build();
            usage = subscriptionUsageRepositoryPort.save(usage);
        }
        return usage;
    }

    private boolean isTrackable(SubscriptionPlanCode planCode, FeatureName feature) {
        return switch (feature) {
            case BOOKINGS -> planCode == SubscriptionPlanCode.BASIC || planCode == SubscriptionPlanCode.FREE_TIER;
            case RESOURCES -> planCode != SubscriptionPlanCode.PROFESSIONAL;
            default -> false;
        };
    }
}