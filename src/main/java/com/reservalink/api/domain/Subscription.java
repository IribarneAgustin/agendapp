package com.reservalink.api.domain;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Subscription {
    private String id;
    private String userId;
    private Boolean expired;
    private LocalDateTime creationDateTime;
    private LocalDateTime expiration;
    private String checkoutLink;
}