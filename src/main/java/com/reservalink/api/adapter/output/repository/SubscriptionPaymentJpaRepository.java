package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SubscriptionPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SubscriptionPaymentJpaRepository extends JpaRepository<SubscriptionPaymentEntity, String> {
    Optional<SubscriptionPaymentEntity> findByExternalId(String externalId);
}