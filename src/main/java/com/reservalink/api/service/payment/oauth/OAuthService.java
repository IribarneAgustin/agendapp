package com.reservalink.api.service.payment.oauth;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

public interface OAuthService {

    String buildAuthorizationUrl(UUID userId) throws UnsupportedEncodingException;

    void exchangeCodeForToken(String code, UUID userId);

    String getValidAccessToken(UUID userId);

    String refreshAccessToken(String refreshToken);

    boolean isUserConnected(UUID userId);
}
