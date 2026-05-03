package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SubscriptionUsageEntity;
import com.reservalink.api.domain.enums.PeriodStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionUsageJpaRepository extends JpaRepository<SubscriptionUsageEntity, String> {
    Optional<SubscriptionUsageEntity> findTopBySubscriptionIdAndPeriodStatusOrderByStartPeriodDateTimeDesc(String subscriptionId, PeriodStatus periodStatus);

    List<SubscriptionUsageEntity> findAllBySubscriptionIdAndPeriodStatus(String subscriptionId, PeriodStatus periodStatus);
}