package com.reservalink.api.service.user;

import com.reservalink.api.controller.request.UserLoginRequest;
import com.reservalink.api.controller.request.UserRegistrationRequest;
import com.reservalink.api.controller.request.UserRequest;
import com.reservalink.api.controller.response.UserAuthResponse;
import com.reservalink.api.domain.User;
import com.reservalink.api.repository.entity.SubscriptionEntity;
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

    void requestPasswordChange(String email);

    @Transactional(rollbackFor = Exception.class)
    void resetPassword(String password, String token);

    SubscriptionEntity findUserSubscriptionByUserEmail(String email);

    boolean isSubscriptionExpired(String userId);
}
