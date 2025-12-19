package com.reservalink.api.controller;

import com.reservalink.api.repository.entity.SubscriptionEntity;
import com.reservalink.api.security.JWTUtils;
import com.reservalink.api.service.user.UserService;
import com.reservalink.api.utils.AuthUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
