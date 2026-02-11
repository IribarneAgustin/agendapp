package com.reservalink.api.adapter.input.controller;

import com.reservalink.api.adapter.output.repository.entity.SubscriptionEntity;
import com.reservalink.api.config.security.JWTUtils;
import com.reservalink.api.application.service.user.UserService;
import com.reservalink.api.utils.AuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/subscription")
public class SubscriptionController {

    private final UserService userService;
    private final JWTUtils jwtUtils;

    public SubscriptionController(UserService userService, JWTUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/expired")
    public String expired(HttpServletRequest request) {

        String token = AuthUtils.getTokenFromCookies(request);
        if (token == null) {
            return "redirect:/";
        }

        String email = jwtUtils.getEmailFromToken(token);
        SubscriptionEntity subscription =
                userService.findUserSubscriptionByUserEmail(email);

        String encodedCheckoutLink = URLEncoder.encode(
                subscription.getCheckoutLink(),
                StandardCharsets.UTF_8
        );

        String encodedExpiration = URLEncoder.encode(
                subscription.getExpiration().toString(),
                StandardCharsets.UTF_8
        );

        return "redirect:/public/subscription-expired.html"
                + "?checkoutLink=" + encodedCheckoutLink
                + "&expirationDate=" + encodedExpiration;
    }


}
