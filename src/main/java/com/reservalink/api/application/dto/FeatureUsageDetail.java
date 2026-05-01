package com.reservalink.api.application.dto;

import com.reservalink.api.domain.enums.FeatureStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FeatureUsageDetail {
    private String id;
    private Boolean enabled;
    private FeatureStatus featureStatus;
    private Integer usage;
    private String usageLimit;
}