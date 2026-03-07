package com.reservalink.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageSession {
    private String id;
    private String offeringId;
    private Integer sessionLimit;
    private Double price;
    private PackageSessionStatus status;
}
