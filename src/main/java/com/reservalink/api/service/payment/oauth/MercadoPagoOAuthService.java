package com.reservalink.api.service.payment.oauth;

import com.reservalink.api.repository.entity.PaymentAccountTokenEntity;
import com.reservalink.api.repository.entity.PaymentMethod;
import com.reservalink.api.repository.entity.UserEntity;
import com.reservalink.api.repository.PaymentAccountTokenRepository;
import com.reservalink.api.repository.UserRepository;
import com.reservalink.api.service.payment.oauth.dto.TokenResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Service
public class MercadoPagoOAuthService implements OAuthService {

    @Value("${mercadopago.client.id}")
    private String clientId;

    @Value("${mercadopago.client.secret}")
    private String clientSecret;

    @Value("${mercadopago.redirect.uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();
    private final PaymentAccountTokenRepository tokenRepository;
    private final UserRepository userRepository;

    public MercadoPagoOAuthService(PaymentAccountTokenRepository tokenRepository, UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    public String buildAuthorizationUrl(UUID userId) {
        String base = "https://auth.mercadopago.com.ar/authorization";
        String params = String.format(
                "response_type=code" +
                        "&client_id=%s" +
                        "&platform_id=mp" +
                        "&state=%s" +
                        "&redirect_uri=%s",
                URLEncoder.encode(clientId, StandardCharsets.UTF_8),
                URLEncoder.encode(userId.toString(), StandardCharsets.UTF_8),
                URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
        );

        return base + "?" + params;
    }

    @Override
    public void exchangeCodeForToken(String code, UUID userId) {
        UserEntity userEntity = userRepository.findById(userId.toString())
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        String url = "https://api.mercadopago.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=authorization_code"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&code=" + code
                + "&redirect_uri=" + redirectUri;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, TokenResponse.class);
            TokenResponse token = response.getBody();

            if (token == null || token.getAccessToken() == null) {
                throw new RuntimeException("Invalid token response from Mercado Pago");
            }

            PaymentAccountTokenEntity paymentToken = tokenRepository.findByUserEntityId(userId.toString())
                    .orElse(new PaymentAccountTokenEntity());

            paymentToken.setUserEntity(userEntity);
            paymentToken.setEnabled(true);
            paymentToken.setPaymentMethod(PaymentMethod.MERCADO_PAGO);
            paymentToken.setAccessToken(token.getAccessToken());
            paymentToken.setRefreshToken(token.getRefreshToken());
            paymentToken.setExpiresAt(System.currentTimeMillis() + (token.getExpiresIn() * 1000));

            tokenRepository.save(paymentToken);

            log.info("Mercado Pago account linked for user {}", userId);

        } catch (Exception e) {
            log.error("Error exchanging code for token for user {}", userId, e);
            throw new RuntimeException("Failed to exchange Mercado Pago OAuth code", e);
        }
    }

    @Override
    public String getValidAccessToken(UUID userId) {
        PaymentAccountTokenEntity token = tokenRepository.findByUserEntityId(userId.toString())
                .orElseThrow(() -> new RuntimeException("User not linked with Mercado Pago"));

        if (token.getExpiresAt() != null && System.currentTimeMillis() > token.getExpiresAt()) {
            log.info("Refreshing expired token for user {}", userId);
            String newAccessToken = refreshAccessToken(token.getRefreshToken());
            token.setAccessToken(newAccessToken);
            token.setExpiresAt(System.currentTimeMillis() + (60 * 60 * 1000));
            tokenRepository.save(token);
        }

        return token.getAccessToken();
    }

    @Override
    public String refreshAccessToken(String refreshToken) {
        String url = "https://api.mercadopago.com/oauth/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=refresh_token"
                + "&client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&refresh_token=" + refreshToken;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.exchange(url, HttpMethod.POST, entity, TokenResponse.class);
            TokenResponse token = response.getBody();

            if (token == null || token.getAccessToken() == null) {
                throw new RuntimeException("Failed to refresh token");
            }

            return token.getAccessToken();
        } catch (Exception e) {
            log.error("Error refreshing Mercado Pago token", e);
            throw new RuntimeException("Failed to refresh Mercado Pago token", e);
        }
    }

    @Override
    public boolean isUserConnected(UUID userId) {
        PaymentAccountTokenEntity token = tokenRepository.findByUserEntityId(userId.toString()).orElse(null);
        return token != null && token.getExpiresAt() != null && System.currentTimeMillis() < token.getExpiresAt();
    }

    @Override
    public void unlink(UUID userId) {
        tokenRepository.findByUserEntityId(userId.toString()).ifPresent(tokenRepository::delete);
    }

}
