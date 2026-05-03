package com.reservalink.api.adapter.input.controller.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SubscriptionUpdateRequest(
        @Min(1)
        @Max(20)
        Integer selectedResources
) {
}