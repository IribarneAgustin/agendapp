package com.reservalink.api.adapter.output.repository.mapper;

import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.adapter.output.repository.entity.BookingPaymentEntity;
import com.reservalink.api.adapter.output.repository.entity.FeaturePaymentEntity;
import com.reservalink.api.adapter.output.repository.entity.FeatureUsageEntity;
import com.reservalink.api.adapter.output.repository.entity.SubscriptionEntity;
import com.reservalink.api.adapter.output.repository.entity.SubscriptionPaymentEntity;
import com.reservalink.api.domain.BookingPayment;
import com.reservalink.api.domain.FeaturePayment;
import com.reservalink.api.domain.SubscriptionPayment;
import org.springframework.stereotype.Component;

@Component
public class PaymentRepositoryMapper {

    public SubscriptionPaymentEntity toEntity(SubscriptionPayment domain) {
        if (domain == null) {
            return null;
        }

        return SubscriptionPaymentEntity.builder()
                .id(domain.getId())
                .amount(domain.getAmount())
                .paymentDate(domain.getPaymentDate())
                .paymentMethod(domain.getPaymentMethod())
                .externalId(domain.getExternalId())
                .paymentStatus(domain.getPaymentStatus())
                .enabled(domain.isEnabled())
                .subscriptionEntity(domain.getSubscriptionId() != null ? SubscriptionEntity.builder().id(domain.getSubscriptionId()).build() : null)
                .build();
    }

    public SubscriptionPayment toDomain(SubscriptionPaymentEntity entity) {
        if (entity == null) {
            return null;
        }

        return SubscriptionPayment.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .paymentDate(entity.getPaymentDate())
                .paymentMethod(entity.getPaymentMethod())
                .externalId(entity.getExternalId())
                .paymentStatus(entity.getPaymentStatus())
                .enabled(entity.getEnabled())
                .subscriptionId(entity.getSubscriptionEntity() != null ? entity.getSubscriptionEntity().getId() : null)
                .build();
    }

    public BookingPaymentEntity toEntity(BookingPayment domain) {
        if (domain == null) {
            return null;
        }

        return BookingPaymentEntity.builder()
                .id(domain.getId())
                .amount(domain.getAmount())
                .paymentDate(domain.getPaymentDate())
                .paymentMethod(domain.getPaymentMethod())
                .externalId(domain.getExternalId())
                .paymentStatus(domain.getPaymentStatus())
                .enabled(domain.isEnabled())
                .bookingEntity(domain.getBookingId() != null ? BookingEntity.builder().id(domain.getBookingId()).build() : null)
                .build();
    }

    public BookingPayment toDomain(BookingPaymentEntity entity) {
        if (entity == null) {
            return null;
        }

        return BookingPayment.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .paymentDate(entity.getPaymentDate())
                .paymentMethod(entity.getPaymentMethod())
                .externalId(entity.getExternalId())
                .paymentStatus(entity.getPaymentStatus())
                .enabled(entity.getEnabled())
                .bookingId(entity.getBookingEntity() != null ? entity.getBookingEntity().getId() : null)
                .build();
    }

    public FeaturePaymentEntity toEntity(FeaturePayment domain) {
        if (domain == null) {
            return null;
        }

        return FeaturePaymentEntity.builder()
                .id(domain.getId())
                .amount(domain.getAmount())
                .paymentDate(domain.getPaymentDate())
                .paymentMethod(domain.getPaymentMethod())
                .externalId(domain.getExternalId())
                .paymentStatus(domain.getPaymentStatus())
                .enabled(domain.isEnabled())
                .featureUsage(domain.getFeatureUsageId() != null ? FeatureUsageEntity.builder().id(domain.getFeatureUsageId()).build() : null)
                .build();
    }

    public FeaturePayment toDomain(FeaturePaymentEntity entity) {
        if (entity == null) {
            return null;
        }

        return FeaturePayment.builder()
                .id(entity.getId())
                .amount(entity.getAmount())
                .paymentDate(entity.getPaymentDate())
                .paymentMethod(entity.getPaymentMethod())
                .externalId(entity.getExternalId())
                .paymentStatus(entity.getPaymentStatus())
                .enabled(entity.getEnabled())
                .featureUsageId(entity.getFeatureUsage() != null ? entity.getFeatureUsage().getId() : null)
                .build();
    }
}