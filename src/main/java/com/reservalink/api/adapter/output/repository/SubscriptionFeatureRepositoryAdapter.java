package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SubscriptionFeatureEntity;
import com.reservalink.api.application.output.SubscriptionFeatureRepositoryPort;
import com.reservalink.api.domain.enums.FeatureName;
import com.reservalink.api.domain.SubscriptionFeature;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class SubscriptionFeatureRepositoryAdapter implements SubscriptionFeatureRepositoryPort {

    private final SubscriptionFeatureJpaRepository subscriptionFeatureJpaRepository;

    public SubscriptionFeatureRepositoryAdapter(
            SubscriptionFeatureJpaRepository subscriptionFeatureJpaRepository) {
        this.subscriptionFeatureJpaRepository = subscriptionFeatureJpaRepository;
    }

    @Override
    public Optional<SubscriptionFeature> findByNameAndUsageLimit(FeatureName name, Integer usageLimit) {
        return subscriptionFeatureJpaRepository
                .findByNameAndUsageLimitAndEnabledTrue(name, usageLimit)
                .map(this::toDomain);
    }

    @Override
    public List<SubscriptionFeature> findAllByIds(List<String> subscriptionFeatureIds) {
        if (subscriptionFeatureIds == null || subscriptionFeatureIds.isEmpty()) {
            return List.of();
        }
        return subscriptionFeatureJpaRepository
                .findByIdInAndEnabledTrue(subscriptionFeatureIds)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    private SubscriptionFeature toDomain(SubscriptionFeatureEntity entity) {
        if (entity == null) {
            return null;
        }

        return SubscriptionFeature.builder()
                .id(entity.getId())
                .name(entity.getName())
                .usageLimit(entity.getUsageLimit())
                .price(entity.getPrice())
                .enabled(entity.getEnabled())
                .build();
    }
}
