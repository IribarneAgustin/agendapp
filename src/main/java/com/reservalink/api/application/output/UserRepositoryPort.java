package com.reservalink.api.application.output;

import com.reservalink.api.domain.Subscription;
import com.reservalink.api.domain.User;

import java.util.Optional;

public interface UserRepositoryPort {
    Optional<Subscription> findUserSubscriptionByUserId(String userId);

    Optional<User> findBySubscriptionId(String userSubscriptionId);
}