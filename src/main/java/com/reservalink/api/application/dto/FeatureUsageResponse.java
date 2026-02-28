package com.reservalink.api.application.dto;

import com.reservalink.api.domain.FeatureUsage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FeatureUsageResponse {
    private FeatureUsage featureUsage;
    private String premiumFeatureCheckoutURL;
}