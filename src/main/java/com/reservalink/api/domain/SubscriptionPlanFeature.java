package com.reservalink.api.domain;

import com.reservalink.api.domain.enums.FeatureName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SubscriptionPlanFeature {
    private FeatureName name;
    private Integer limit;
    private Boolean enabled;
}