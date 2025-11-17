package com.agendapp.api.service.user;

import com.agendapp.api.controller.request.UserLoginRequest;
import com.agendapp.api.controller.request.UserRegistrationRequest;
import com.agendapp.api.controller.request.UserRequest;
import com.agendapp.api.controller.response.UserAuthResponse;
import com.agendapp.api.domain.User;
import com.agendapp.api.repository.entity.SubscriptionEntity;
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
