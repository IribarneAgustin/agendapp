package com.reservalink.api.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionStatusResponse {
    private String planName;
    private List<SubscriptionUsageResponse> features;
    private SubscriptionPlanBilling billing;
    private String checkoutLink;
}