package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SubscriptionFeatureEntity;
import com.reservalink.api.domain.FeatureName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionFeatureJpaRepository extends JpaRepository<SubscriptionFeatureEntity, String> {
    Optional<SubscriptionFeatureEntity> findByNameAndUsageLimitAndEnabledTrue(FeatureName name, Integer usageLimit);

    Optional<SubscriptionFeatureEntity> findByIdInAndEnabledTrue(List<String> subscriptionFeatureIds);
}