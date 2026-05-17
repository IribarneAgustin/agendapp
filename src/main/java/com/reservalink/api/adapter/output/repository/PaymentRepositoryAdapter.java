package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.BookingPaymentEntity;
import com.reservalink.api.adapter.output.repository.entity.FeaturePaymentEntity;
import com.reservalink.api.adapter.output.repository.entity.SubscriptionPaymentEntity;
import com.reservalink.api.adapter.output.repository.mapper.PaymentRepositoryMapper;
import com.reservalink.api.application.output.PaymentRepositoryPort;
import com.reservalink.api.domain.BookingPayment;
import com.reservalink.api.domain.FeaturePayment;
import com.reservalink.api.domain.SubscriptionPayment;
import com.reservalink.api.domain.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final PaymentRepository paymentJpaRepository;
    private final SubscriptionPaymentJpaRepository subscriptionPaymentJpaRepository;
    private final FeaturePaymentJpaRepository featurePaymentJpaRepository;
    private final PaymentRepositoryMapper paymentRepositoryMapper;

    @Override
    public SubscriptionPayment save(SubscriptionPayment subscriptionPayment) {
        SubscriptionPaymentEntity entity = paymentRepositoryMapper.toEntity(subscriptionPayment);
        SubscriptionPaymentEntity savedEntity = paymentJpaRepository.saveAndFlush(entity);
        return paymentRepositoryMapper.toDomain(savedEntity);
    }

    @Override
    public BookingPayment save(BookingPayment bookingPayment) {
        BookingPaymentEntity entity = paymentRepositoryMapper.toEntity(bookingPayment);
        BookingPaymentEntity savedEntity = paymentJpaRepository.saveAndFlush(entity);
        return paymentRepositoryMapper.toDomain(savedEntity);
    }

    @Override
    public FeaturePayment save(FeaturePayment featurePayment) {
        FeaturePaymentEntity entity = paymentRepositoryMapper.toEntity(featurePayment);
        FeaturePaymentEntity savedEntity = paymentJpaRepository.saveAndFlush(entity);
        return paymentRepositoryMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<BookingPayment> findByExternalId(String externalId) {
        return paymentJpaRepository.findByExternalId(externalId).map(paymentRepositoryMapper::toDomain);
    }

    @Override
    public Optional<SubscriptionPayment> findSubscriptionPaymentByExternalId(String externalId) {
        return subscriptionPaymentJpaRepository.findByExternalId(externalId).map(paymentRepositoryMapper::toDomain);
    }

    @Override
    public Boolean existsByExternalIdAndPaymentStatus(String externalId, PaymentStatus paymentStatus) {
        return paymentJpaRepository.existsByEnabledTrueAndExternalIdAndPaymentStatus(externalId, paymentStatus);
    }

    @Override
    public Optional<FeaturePayment> findFeaturePaymentByExternalId(String externalId) {
        return featurePaymentJpaRepository.findByExternalId(externalId).map(paymentRepositoryMapper::toDomain);
    }
}