package com.reservalink.api.adapter.input.controller.request;

import com.reservalink.api.domain.enums.FeatureName;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FeatureUsageRequest(

        @NotNull
        FeatureName name,

        @Positive
        Integer usageLimit,

        Boolean enabled
) {
}