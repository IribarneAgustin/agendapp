package com.reservateya.api.controller;

import com.reservateya.api.controller.request.UserRequest;
import com.reservateya.api.controller.response.SubscriptionResponse;
import com.reservateya.api.repository.entity.SubscriptionEntity;
import com.reservateya.api.domain.User;
import com.reservateya.api.service.user.UserService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    public UserController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> findUserById(@PathVariable UUID userId) {
        User user = userService.findById(userId);
        return ResponseEntity.ok().body(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> update(@PathVariable UUID userId, @Valid @RequestBody UserRequest request) {
        User user = userService.update(userId, request);
        return ResponseEntity.ok().body(user);
    }

    @GetMapping("/{userId}/public-url")
    public ResponseEntity<String> getPublicURL(@PathVariable UUID userId) {
        String url = userService.getPublicURL(userId);
        return ResponseEntity.ok().body(url);
    }

    @GetMapping("/{userId}/subscription")
    public ResponseEntity<SubscriptionResponse> findUserSubscription(@PathVariable UUID userId) {
        SubscriptionEntity subscriptionEntity = userService.findUserSubscription(userId);
        return ResponseEntity.ok().body(modelMapper.map(subscriptionEntity, SubscriptionResponse.class));
    }

}
