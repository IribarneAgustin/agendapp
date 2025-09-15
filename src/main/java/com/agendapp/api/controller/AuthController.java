package com.agendapp.api.controller;

import com.agendapp.api.controller.request.UserLoginRequest;
import com.agendapp.api.controller.request.UserRegistrationRequest;
import com.agendapp.api.controller.response.UserAuthResponse;
import com.agendapp.api.dto.UserDTO;
import com.agendapp.api.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Slf4j
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    public AuthController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserAuthResponse register(@RequestBody @Valid UserRegistrationRequest userRegistrationRequest) {
        log.info("User registration request received");
        UserDTO newUser = userService.register(userRegistrationRequest);
        return modelMapper.map(newUser, UserAuthResponse.class);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public UserAuthResponse login(@RequestBody @Validated UserLoginRequest userLoginRequest){
        log.info("User login request received");
        return userService.login(userLoginRequest);
    }
}
