package com.agendapp.api.controller;

import com.agendapp.api.service.payment.oauth.OAuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
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
    public ResponseEntity<Void> handleCallback(@RequestParam String code, @RequestParam UUID state, HttpServletResponse response) throws IOException {
        try {
            oAuthService.exchangeCodeForToken(code, state);
            String redirectUrl = baseURL + "/admin/dashboard.html?linked=true";
            response.sendRedirect(redirectUrl);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error linking Mercado Pago account for user {}", state, e);
            String redirectUrl = baseURL +"/admin/dashboard.html?linked=false";
            response.sendRedirect(redirectUrl);
            return ResponseEntity.status(500).build();
        }
    }


}
