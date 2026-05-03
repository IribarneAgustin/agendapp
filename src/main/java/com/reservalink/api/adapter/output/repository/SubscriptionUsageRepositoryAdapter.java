package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SubscriptionUsageEntity;
import com.reservalink.api.adapter.output.repository.mapper.SubscriptionUsageRepositoryMapper;
import com.reservalink.api.application.output.SubscriptionUsageRepositoryPort;
import com.reservalink.api.domain.SubscriptionUsage;
import com.reservalink.api.domain.enums.PeriodStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubscriptionUsageRepositoryAdapter implements SubscriptionUsageRepositoryPort {

    private final SubscriptionUsageJpaRepository subscriptionUsageJpaRepository;
    private final SubscriptionUsageRepositoryMapper subscriptionUsageRepositoryMapper;

    @Override
    public Optional<SubscriptionUsage> findCurrentBySubscriptionId(String subscriptionId) {
        return subscriptionUsageJpaRepository
                .findTopBySubscriptionIdAndPeriodStatusOrderByStartPeriodDateTimeDesc(subscriptionId, PeriodStatus.ACTIVE)
                .map(subscriptionUsageRepositoryMapper::toDomain);
    }

    @Override
    public SubscriptionUsage save(SubscriptionUsage domain) {
        SubscriptionUsageEntity entity = subscriptionUsageRepositoryMapper.toEntity(domain);
        SubscriptionUsageEntity saved = subscriptionUsageJpaRepository.save(entity);
        return subscriptionUsageRepositoryMapper.toDomain(saved);
    }

    @Override
    public List<SubscriptionUsage> findAllBySubscriptionIdAndPeriodStatus(String subscriptionId, PeriodStatus periodStatus) {
        return subscriptionUsageJpaRepository
                .findAllBySubscriptionIdAndPeriodStatus(subscriptionId, periodStatus)
                .stream()
                .map(subscriptionUsageRepositoryMapper::toDomain)
                .toList();
    }
}