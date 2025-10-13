package com.agendapp.api.service;

import com.agendapp.api.controller.request.UserLoginRequest;
import com.agendapp.api.controller.request.UserRegistrationRequest;
import com.agendapp.api.controller.request.UserRequest;
import com.agendapp.api.controller.response.UserAuthResponse;
import com.agendapp.api.dto.UserDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface UserService {

    @Transactional(rollbackFor = Exception.class)
    UserDTO register(UserRegistrationRequest userRegistrationRequest);

    UserAuthResponse login(UserLoginRequest userLoginRequest);

    String findUserIdByBrandName(String brandName);

    UserDTO findById(UUID id);

    UserDTO update(UUID id, UserRequest userRequest);

    String getPublicURL(UUID userId);
}
