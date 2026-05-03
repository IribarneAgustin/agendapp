package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SubscriptionPlanEntity;
import com.reservalink.api.domain.enums.SubscriptionPlanCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionPlanJpaRepository extends JpaRepository<SubscriptionPlanEntity, String> {

    @Query("""
                SELECT u.subscriptionEntity.subscriptionPlan
                FROM UserEntity u
                WHERE u.id = :userId
                AND u.subscriptionEntity.enabled = true
                AND u.subscriptionEntity.subscriptionPlan.enabled = true
            """)
    Optional<SubscriptionPlanEntity> findByUserId(String userId);

    Optional<SubscriptionPlanEntity> findByCode(SubscriptionPlanCode code);

    Optional<SubscriptionPlanEntity> findByIdAndEnabledTrue(String planId);
}