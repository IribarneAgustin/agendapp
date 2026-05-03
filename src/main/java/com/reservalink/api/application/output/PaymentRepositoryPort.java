package com.reservalink.api.application.output;

import com.reservalink.api.domain.BookingPayment;
import com.reservalink.api.domain.SubscriptionPayment;

import java.util.Optional;

public interface PaymentRepositoryPort {
    SubscriptionPayment save(SubscriptionPayment subscriptionPayment);

    BookingPayment save(BookingPayment bookingPayment);

    Optional<BookingPayment> findByExternalId(String externalId);
}