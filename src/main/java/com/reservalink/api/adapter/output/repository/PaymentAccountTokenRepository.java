package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.PaymentAccountTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentAccountTokenRepository extends JpaRepository<PaymentAccountTokenEntity, String> {
    Optional<PaymentAccountTokenEntity> findByUserEntityId(String userId);
}
