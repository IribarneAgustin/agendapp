package com.agendapp.api.controller;

import com.agendapp.api.controller.request.UserRequest;
import com.agendapp.api.dto.UserDTO;
import com.agendapp.api.service.UserService;
import jakarta.validation.Valid;
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


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UserDTO> findUserById(@PathVariable UUID userId) {
        UserDTO user = userService.findById(userId);
        return ResponseEntity.ok().body(user);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserDTO> update(@PathVariable UUID userId, @Valid @RequestBody UserRequest request) {
        UserDTO user = userService.update(userId, request);
        return ResponseEntity.ok().body(user);
    }

    @GetMapping("/{userId}/public-url")
    public ResponseEntity<String> getPublicURL(@PathVariable UUID userId) {
        String url = userService.getPublicURL(userId);
        return ResponseEntity.ok().body(url);
    }

}
