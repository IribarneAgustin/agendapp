package com.reservalink.api.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SubscriptionAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException ex) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        boolean expired = auth != null &&
                auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority()
                                .equals(Authority.SUBSCRIPTION_EXPIRED.name()));

        if (expired) {
            response.sendRedirect("/subscription/expired");
            return;
        }

        response.setStatus(HttpStatus.FORBIDDEN.value());
    }
}
