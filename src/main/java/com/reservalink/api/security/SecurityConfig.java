package com.reservalink.api.security;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final SubscriptionAccessDeniedHandler subscriptionAccessDeniedHandler;

    private static final List<String> publicEndpointList = List.of(
            "/auth/**",
            "/booking",
            "/booking/*/cancel",
            "/servicios/**",
            "/slot-time/offering/**",
            "/mercadopago/oauth/callback",
            "/payment/mercadopago/webhook",
            "/users/reset-password",
            "/auth/forgot-password",
            "/subscription/expired",
            "/resource/user/*/offering/*",

            "/",
            "/landing.html",
            "/favicon.ico",
            "/index.html",
            "/commons/config.js",
            "/public/**",
            "/test/**"

    );

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, SubscriptionAccessDeniedHandler subscriptionAccessDeniedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.subscriptionAccessDeniedHandler = subscriptionAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    //because we have multiples verbs for the same path, so publicEndpointList doesn't fit
                    auth.requestMatchers(HttpMethod.GET, "/users/*/offerings").permitAll();

                    publicEndpointList.forEach(endpoint ->
                            auth.requestMatchers(endpoint).permitAll()
                    );

                    auth.anyRequest().hasAuthority(Authority.SUBSCRIPTION_ACTIVE.name());
                })
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(subscriptionAccessDeniedHandler)
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(form -> form
                        .loginPage("/")
                        .defaultSuccessUrl("/", true)
                )
                .build();
    }

}
