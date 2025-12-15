package com.reservalink.api.controller;

import com.reservalink.api.controller.request.ForgotPasswordRequest;
import com.reservalink.api.controller.request.UserLoginRequest;
import com.reservalink.api.controller.request.UserRegistrationRequest;
import com.reservalink.api.controller.response.UserAuthResponse;
import com.reservalink.api.domain.User;
import com.reservalink.api.service.user.UserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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
        User newUser = userService.register(userRegistrationRequest);
        return modelMapper.map(newUser, UserAuthResponse.class);
    }

    @PostMapping("/login")
    public ResponseEntity<UserAuthResponse> login(@RequestBody UserLoginRequest request,
                                                  HttpServletResponse response) {
        log.info("User login request received");
        UserAuthResponse authResponse = userService.login(request);

        ResponseCookie cookie = ResponseCookie.from("jwt", authResponse.getToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(7 * 24 * 60 * 60)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        log.info("User logged out successfully");
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public void sendRecoverPasswordRequest(@RequestBody @Valid ForgotPasswordRequest request) {
        log.info("Recover password request received for the email {}", request.email());
        userService.requestPasswordChange(request.email());
    }

}
