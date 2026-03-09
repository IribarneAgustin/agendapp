package com.reservalink.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageSession {
    private String id;
    private String offeringId;
    private String offeringName;
    private String userId;
    private Integer advancePaymentPercentage;
    private Integer sessionLimit;
    private BigDecimal price;
    private PackageSessionStatus status;
    private boolean enabled;
}
