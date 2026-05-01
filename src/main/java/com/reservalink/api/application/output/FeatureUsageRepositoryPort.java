package com.reservalink.api.application.output;

import com.reservalink.api.domain.enums.FeatureName;
import com.reservalink.api.domain.enums.FeatureStatus;
import com.reservalink.api.domain.FeatureUsage;

import java.util.List;
import java.util.Optional;

public interface FeatureUsageRepositoryPort {
    Optional<FeatureUsage> findByUserSubscriptionIdAndFeatureNameAndStatus(String userSubscriptionId, FeatureName featureName, FeatureStatus featureStatus);

    FeatureUsage create(FeatureUsage featureUsage);

    Optional<FeatureUsage> findById(String featureUsageId);

    FeatureUsage update(FeatureUsage featureUsage);

    Optional<FeatureUsage> findLatestActiveAvailableBySubscriptionIdAndFeatureName(String subscriptionId, FeatureName featureName);

    List<FeatureUsage> findAllById(List<String> featureUsageIds);

    List<FeatureUsage> findAllAvailableByUserId(String userId);

}