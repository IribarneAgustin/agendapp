package com.reservateya.api.service.user;

import com.reservateya.api.controller.request.UserLoginRequest;
import com.reservateya.api.controller.request.UserRegistrationRequest;
import com.reservateya.api.controller.request.UserRequest;
import com.reservateya.api.controller.response.UserAuthResponse;
import com.reservateya.api.domain.User;
import com.reservateya.api.repository.entity.SubscriptionEntity;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface UserService {

    @Transactional(rollbackFor = Exception.class)
    User register(UserRegistrationRequest userRegistrationRequest);

    UserAuthResponse login(UserLoginRequest userLoginRequest);

    String findUserIdByBrandName(String brandName);

    User findById(UUID id);

    User update(UUID id, UserRequest userRequest);

    String getPublicURL(UUID userId);

    SubscriptionEntity findUserSubscription(UUID userId);
}
