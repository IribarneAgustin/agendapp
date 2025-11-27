package com.reservalink.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class OfferingResponse {

    private UUID id;

    private UUID userId;

    private String name;

    private String description;

    private Integer capacity;

    private Integer advancePaymentPercentage;

    private Boolean enabled;
}
