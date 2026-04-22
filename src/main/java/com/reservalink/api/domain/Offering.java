package com.reservalink.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Offering {
    private String id;
    private String userId;
    private String name;
    private String description;
    private Integer capacity;
    private Integer advancePaymentPercentage;
    private Boolean status;
    private String termsAndConditions;
    private boolean enabled;
    private String categoryId;
    private Integer displayOrder;
}