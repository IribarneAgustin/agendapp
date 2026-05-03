package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.SubscriptionEntity;
import com.reservalink.api.adapter.output.repository.entity.UserEntity;
import com.reservalink.api.application.output.UserRepositoryPort;
import com.reservalink.api.domain.Subscription;
import com.reservalink.api.domain.User;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;

    public UserRepositoryAdapter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<Subscription> findUserSubscriptionByUserId(String userId) {
        return userRepository.findByUserIdAndEnabledTrue(userId).map(this::toDomain);
    }

    @Override
    public Optional<User> findBySubscriptionId(String userSubscriptionId) {
        UserEntity userEntity = userRepository.findBySubscriptionEntity_IdAndEnabledTrue(userSubscriptionId).orElse(null);
        return Optional.ofNullable(toDomain(userEntity));
    }

    @Override
    public Optional<User> findById(String userId) {
        return userRepository.findByIdAndEnabledTrue(userId).map(this::toDomain);
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
                .subscriptionPlanId(entity.getSubscriptionPlan().getId())
                .selectedResourcesLimit(entity.getSelectedResourcesLimit())
                .build();
    }

    private User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.builder()
                .id(entity.getId())
                .name(entity.getName())
                .lastName(entity.getLastName())
                .enabled(entity.getEnabled())
                .email(entity.getEmail())
                .brandName(entity.getBrandEntity().getName())//FIXME (Change by id)
                .phone(entity.getPhone())
                .subscriptionId(entity.getSubscriptionEntity().getId())
                .build();
    }

}