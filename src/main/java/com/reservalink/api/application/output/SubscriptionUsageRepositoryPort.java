package com.reservalink.api.application.output;

import com.reservalink.api.domain.SubscriptionUsage;
import com.reservalink.api.domain.enums.PeriodStatus;

import java.util.List;
import java.util.Optional;

public interface SubscriptionUsageRepositoryPort {
    Optional<SubscriptionUsage> findCurrentBySubscriptionId(String subscriptionId);

    SubscriptionUsage save(SubscriptionUsage subscriptionPlanUsage);

    List<SubscriptionUsage> findAllBySubscriptionIdAndPeriodStatus(String subscriptionId, PeriodStatus periodStatus);
}