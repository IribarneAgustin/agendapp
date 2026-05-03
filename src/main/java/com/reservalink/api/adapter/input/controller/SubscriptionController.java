package com.reservalink.api.adapter.input.controller;

import com.reservalink.api.adapter.input.controller.request.SubscriptionUpdateRequest;
import com.reservalink.api.adapter.output.repository.entity.SubscriptionEntity;
import com.reservalink.api.adapter.output.repository.entity.SubscriptionPlanEntity;
import com.reservalink.api.application.dto.SubscriptionStatusResponse;
import com.reservalink.api.application.service.payment.CheckoutService;
import com.reservalink.api.application.service.subscription.SubscriptionPlanService;
import com.reservalink.api.application.service.user.UserService;
import com.reservalink.api.config.security.JWTUtils;
import com.reservalink.api.domain.enums.SubscriptionPlanCode;
import com.reservalink.api.utils.AuthUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Controller
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final UserService userService;
    private final JWTUtils jwtUtils;
    private final SubscriptionPlanService subscriptionPlanService;
    private final CheckoutService checkoutService;

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

        SubscriptionPlanEntity subscriptionPlan = subscription.getSubscriptionPlan();
        String redirectString = "redirect:/public/subscription-expired.html"
                + "?checkoutLink=" + encodedCheckoutLink
                + "&expirationDate=" + encodedExpiration;

        if (SubscriptionPlanCode.FREE_TIER == subscriptionPlan.getCode()) {
            redirectString = "redirect:/public/subscription-plans.html";
        }

        return redirectString;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<SubscriptionStatusResponse> findSubscriptionStatus(@PathVariable UUID userId) {
        SubscriptionStatusResponse response = subscriptionPlanService.findSubscriptionStatus(userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{planCode}/user/{userId}")
    public ResponseEntity<String> createSubscriptionPlan(@PathVariable SubscriptionPlanCode planCode, @PathVariable UUID userId, @RequestBody SubscriptionUpdateRequest request) {
        String checkoutUrl = checkoutService.createSubscriptionCheckoutUrl(userId.toString(), planCode, request.selectedResources());
        return ResponseEntity.ok(checkoutUrl);
    }
}
