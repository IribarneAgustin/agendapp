package com.reservalink.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FeatureUsage {
    private String id;
    private String subscriptionId;
    private String subscriptionFeatureId;
    private Boolean enabled;
    private FeatureStatus featureStatus;
    private Integer usage;
}