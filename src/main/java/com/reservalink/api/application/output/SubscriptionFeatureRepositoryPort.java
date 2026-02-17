package com.reservalink.api.application.output;

import com.reservalink.api.domain.FeatureName;
import com.reservalink.api.domain.SubscriptionFeature;

import java.util.List;
import java.util.Optional;

public interface SubscriptionFeatureRepositoryPort {
    Optional<SubscriptionFeature> findByNameAndUsageLimit(FeatureName name, Integer integer);

    List<SubscriptionFeature> findAllByIds(List<String> subscriptionFeatureIds);
}