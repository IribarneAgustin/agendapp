package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SubscriptionPlanEntity;
import com.reservalink.api.adapter.output.repository.mapper.SubscriptionPlanMapper;
import com.reservalink.api.application.output.SubscriptionPlanRepositoryPort;
import com.reservalink.api.domain.SubscriptionPlan;
import com.reservalink.api.domain.enums.SubscriptionPlanCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SubscriptionPlanRepositoryAdapter implements SubscriptionPlanRepositoryPort {

    private final SubscriptionPlanJpaRepository subscriptionPlanJpaRepository;
    private final SubscriptionPlanMapper subscriptionPlanMapper;

    @Override
    public Optional<SubscriptionPlan> findByUserId(UUID userId) {
        return subscriptionPlanJpaRepository.findByUserId(userId.toString())
                .map(subscriptionPlanMapper::toDomain);
    }

    @Override
    public SubscriptionPlan save(SubscriptionPlan userPlan) {
        SubscriptionPlanEntity entity = subscriptionPlanMapper.toEntity(userPlan);
        SubscriptionPlanEntity savedEntity = subscriptionPlanJpaRepository.save(entity);
        return subscriptionPlanMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<SubscriptionPlan> findByCode(SubscriptionPlanCode planCode) {
        return subscriptionPlanJpaRepository.findByCode(planCode)
                .map(subscriptionPlanMapper::toDomain);
    }

    @Override
    public Optional<SubscriptionPlan> findById(String planId) {
        return subscriptionPlanJpaRepository.findByIdAndEnabledTrue(planId)
                .map(subscriptionPlanMapper::toDomain);
    }
}