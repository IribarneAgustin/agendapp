package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.BookingPaymentEntity;
import com.reservalink.api.adapter.output.repository.entity.PaymentEntity;
import com.reservalink.api.domain.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
    Optional<BookingPaymentEntity> findByExternalId(String externalId);

    Boolean existsByEnabledTrueAndExternalIdAndPaymentStatus(String externalId, PaymentStatus paymentStatus);
}
