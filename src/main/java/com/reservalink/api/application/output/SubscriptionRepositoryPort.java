package com.reservalink.api.application.output;

import com.reservalink.api.domain.Subscription;

import java.util.Optional;

public interface SubscriptionRepositoryPort {
    Optional<Subscription> findById(String subscriptionId);

    Subscription update(Subscription subscription);
}