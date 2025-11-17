package com.agendapp.api.repository;

import com.agendapp.api.repository.entity.BookingPaymentEntity;
import com.agendapp.api.repository.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, String> {
    Optional<BookingPaymentEntity> findByExternalId(String externalId);
}
