package com.agendapp.api.service;

import com.agendapp.api.controller.request.UserLoginRequest;
import com.agendapp.api.controller.request.UserRegistrationRequest;
import com.agendapp.api.controller.response.UserAuthResponse;
import com.agendapp.api.dto.UserDTO;

public interface UserService {
    UserDTO register(UserRegistrationRequest userRegistrationRequest);

    UserAuthResponse login(UserLoginRequest userLoginRequest);
}
