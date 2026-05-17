package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.FeaturePaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeaturePaymentJpaRepository extends JpaRepository<FeaturePaymentEntity, String> {
    Optional<FeaturePaymentEntity> findByExternalId(String externalId);
}