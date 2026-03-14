package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SubscriptionEntity;
import com.reservalink.api.application.output.SubscriptionRepositoryPort;
import com.reservalink.api.domain.Subscription;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SubscriptionRepositoryAdapter implements SubscriptionRepositoryPort {

    private final SubscriptionRepository jpaRepository;

    public SubscriptionRepositoryAdapter(SubscriptionRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Subscription> findById(String subscriptionId) {
        return jpaRepository.findById(subscriptionId)
                .map(this::toDomain);
    }

    @Override
    public Subscription update(Subscription subscription) {
        SubscriptionEntity entity = jpaRepository.findById(subscription.getId())
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found"));

        entity.setExpired(subscription.getExpired());
        entity.setCheckoutLink(subscription.getCheckoutLink());
        entity.setExpiration(subscription.getExpiration());

        SubscriptionEntity saved = jpaRepository.saveAndFlush(entity);

        return toDomain(saved);
    }

    @Override
    public String findActiveSubscriptionIdByBookingId(String bookingId) {
        return jpaRepository.findSubscriptionIdByBookingId(bookingId);
    }

    private Subscription toDomain(SubscriptionEntity entity) {
        if (entity == null) {
            return null;
        }
        return Subscription.builder()
                .id(entity.getId())
                .expired(entity.isExpired())
                .creationDateTime(entity.getCreationDateTime())
                .expiration(entity.getExpiration())
                .checkoutLink(entity.getCheckoutLink())
                .build();
    }
}
