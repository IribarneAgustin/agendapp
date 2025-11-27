package com.reservalink.api.controller.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SubscriptionResponse {
    private String id;
    private Boolean expired;
    private LocalDateTime creationDateTime;
    private LocalDateTime expiration;
    private String checkoutLink;
}
