package com.reservalink.api.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionUsageResponse {
    private String featureName;
    private Integer used;
    private Integer limit;
}