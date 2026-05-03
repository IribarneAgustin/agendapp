package com.reservalink.api.application.service.subscription;

import com.reservalink.api.application.dto.SubscriptionStatusResponse;

import java.util.UUID;

public interface SubscriptionPlanService {
    SubscriptionStatusResponse findSubscriptionStatus(UUID userId);
}