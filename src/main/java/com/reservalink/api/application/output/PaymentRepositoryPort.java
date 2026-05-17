package com.reservalink.api.application.output;

import com.reservalink.api.domain.BookingPayment;
import com.reservalink.api.domain.FeaturePayment;
import com.reservalink.api.domain.SubscriptionPayment;
import com.reservalink.api.domain.enums.PaymentStatus;

import java.util.Optional;

public interface PaymentRepositoryPort {
    SubscriptionPayment save(SubscriptionPayment subscriptionPayment);

    BookingPayment save(BookingPayment bookingPayment);

    FeaturePayment save(FeaturePayment featurePayment);

    Optional<BookingPayment> findByExternalId(String externalId);

    Optional<SubscriptionPayment> findSubscriptionPaymentByExternalId(String externalId);

    Boolean existsByExternalIdAndPaymentStatus(String externalId, PaymentStatus paymentStatus);

    Optional<FeaturePayment> findFeaturePaymentByExternalId(String externalId);
}