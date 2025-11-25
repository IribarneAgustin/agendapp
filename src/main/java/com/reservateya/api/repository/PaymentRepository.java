package com.reservateya.api.repository;

import com.reservateya.api.repository.entity.BookingPaymentEntity;
import com.reservateya.api.repository.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<BookingPaymentEntity> findByExternalId(String externalId);
}
