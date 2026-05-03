package com.reservalink.api.application.output;

import com.reservalink.api.domain.SubscriptionPlan;
import com.reservalink.api.domain.enums.SubscriptionPlanCode;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionPlanRepositoryPort {
    Optional<SubscriptionPlan> findByUserId(UUID userId);

    SubscriptionPlan save(SubscriptionPlan userPlan);

    Optional<SubscriptionPlan> findByCode(SubscriptionPlanCode planCode);

    Optional<SubscriptionPlan> findById(String planId);
}