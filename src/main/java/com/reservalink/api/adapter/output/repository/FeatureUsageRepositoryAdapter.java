package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.FeatureUsageEntity;
import com.reservalink.api.adapter.output.repository.entity.SubscriptionEntity;
import com.reservalink.api.adapter.output.repository.entity.SubscriptionFeatureEntity;
import com.reservalink.api.application.output.FeatureUsageRepositoryPort;
import com.reservalink.api.domain.enums.FeatureName;
import com.reservalink.api.domain.enums.FeatureStatus;
import com.reservalink.api.domain.FeatureUsage;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class FeatureUsageRepositoryAdapter implements FeatureUsageRepositoryPort {

    private final FeatureUsageJpaRepository featureUsageJpaRepository;

    public FeatureUsageRepositoryAdapter(FeatureUsageJpaRepository featureUsageJpaRepository) {
        this.featureUsageJpaRepository = featureUsageJpaRepository;
    }

    @Override
    public Optional<FeatureUsage> findByUserSubscriptionIdAndFeatureNameAndStatus(String userSubscriptionId,FeatureName featureName, FeatureStatus featureStatus) {
        return featureUsageJpaRepository
                .findByEnabledTrueAndSubscriptionEntity_IdAndSubscriptionFeatureEntity_NameAndFeatureStatus(userSubscriptionId, featureName, featureStatus)
                .map(this::toDomain);
    }

    @Override
    public FeatureUsage create(FeatureUsage featureUsage) {
        FeatureUsageEntity entity = FeatureUsageEntity.builder()
                .subscriptionEntity(SubscriptionEntity.builder().id(featureUsage.getSubscriptionId()).build())
                .subscriptionFeatureEntity(SubscriptionFeatureEntity.builder().id(featureUsage.getSubscriptionFeatureId()).build())
                .featureStatus(featureUsage.getFeatureStatus())
                .usage(featureUsage.getUsage())
                .isFirstCycle(featureUsage.isFirstCycle())
                .activatedAt(featureUsage.getActivatedAt())
                .enabled(featureUsage.getEnabled())
                .build();
        FeatureUsageEntity saved = featureUsageJpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<FeatureUsage> findById(String featureUsageId) {
        return featureUsageJpaRepository.findByIdAndEnabledTrue(featureUsageId).map(this::toDomain);
    }

    @Override
    public FeatureUsage update(FeatureUsage featureUsage) {
        FeatureUsageEntity entity = featureUsageJpaRepository
                .findById(featureUsage.getId()).orElseThrow(() -> new IllegalArgumentException("FeatureUsage not found with id: " + featureUsage.getId()));
        entity.setFeatureStatus(featureUsage.getFeatureStatus());
        entity.setUsage(featureUsage.getUsage());
        entity.setEnabled(featureUsage.getEnabled());
        entity.setSubscriptionEntity(SubscriptionEntity.builder().id(featureUsage.getSubscriptionId()).build());
        entity.setSubscriptionFeatureEntity(SubscriptionFeatureEntity.builder().id(featureUsage.getSubscriptionFeatureId()).build());
        entity.setActivatedAt(featureUsage.getActivatedAt());
        entity.setIsFirstCycle(featureUsage.isFirstCycle());
        FeatureUsageEntity updated = featureUsageJpaRepository.save(entity);
        return toDomain(updated);
    }

    @Override
    public Optional<FeatureUsage> findLatestActiveAvailableBySubscriptionIdAndFeatureName(String subscriptionId, FeatureName featureName) {
        return featureUsageJpaRepository.findLatestActiveAvailable(subscriptionId, featureName)
                .stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public List<FeatureUsage> findAllById(List<String> featureUsageIds) {
        return featureUsageJpaRepository.findAllById(featureUsageIds).stream().map(this::toDomain).toList();
    }

    @Override
    public List<FeatureUsage> findAllAvailableByUserId(String userId) {
        return Optional.ofNullable(featureUsageJpaRepository.findAllActiveByUserId(userId))
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toDomain)
                .toList();
    }


    private FeatureUsage toDomain(FeatureUsageEntity entity) {
        return Optional.ofNullable(entity)
                .map(e -> FeatureUsage.builder()
                        .id(e.getId())
                        .subscriptionId(
                                Optional.ofNullable(e.getSubscriptionEntity())
                                        .map(SubscriptionEntity::getId)
                                        .orElse(null)
                        )
                        .subscriptionFeatureId(
                                Optional.ofNullable(e.getSubscriptionFeatureEntity())
                                        .map(SubscriptionFeatureEntity::getId)
                                        .orElse(null)
                        )
                        .featureStatus(e.getFeatureStatus())
                        .usage(e.getUsage())
                        .enabled(e.getEnabled())
                        .activatedAt(e.getActivatedAt())
                        .firstCycle(e.getIsFirstCycle())
                        .build()
                )
                .orElse(null);
    }

}