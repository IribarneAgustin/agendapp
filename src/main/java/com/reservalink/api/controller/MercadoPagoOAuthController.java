package com.reservalink.api.controller;

import com.reservalink.api.service.payment.oauth.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ClassPathResource;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/mercadopago/oauth")
@RequiredArgsConstructor
public class MercadoPagoOAuthController {

    private final OAuthService oAuthService;
    @Value("${api.base.url}")
    private String baseURL;
    /**
     * Returns Mercado Pago account connection status for a user.
     */
    @GetMapping("/status/{userId}")
    public ResponseEntity<Map<String, Object>> getConnectionStatus(@PathVariable UUID userId) {
        try {
            boolean connected = oAuthService.isUserConnected(userId);
            return ResponseEntity.ok(Map.of("connected", connected));
        } catch (Exception e) {
            log.error("Error checking Mercado Pago connection status for user {}", userId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "connected", false,
                    "error", "Error checking Mercado Pago connection status"
            ));
        }
    }

    /**
     * Builds Mercado Pago OAuth authorization URL for a user.
     */
    @GetMapping("/link")
    public ResponseEntity<Map<String, String>> linkAccount(@RequestParam UUID userId) {
        try {
            String authUrl = oAuthService.buildAuthorizationUrl(userId);
            return ResponseEntity.ok(Map.of("authUrl", authUrl));
        } catch (Exception e) {
            log.error("Error building Mercado Pago OAuth link for user {}", userId, e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error creating Mercado Pago OAuth link"
            ));
        }
    }

    /**
     * Callback endpoint that Mercado Pago redirects to after authorization.
     */
    @GetMapping("/callback")
    public String handleCallback(@RequestParam String code, @RequestParam UUID state) {
        try {
            oAuthService.exchangeCodeForToken(code, state);
            return "redirect:" + baseURL + "?linked=true";
        } catch (Exception e) {
            log.error("Error linking Mercado Pago account for user {}", state, e);
            return "redirect:" + baseURL + "?linked=false";
        }
    }




}
