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
public class BookingPackage {
    private String id;
    private String packageSessionId;
    private String customerEmail;
    private String customerName;
    private String customerPhone;
    private Integer sessionsUsed;
    private Integer sessionsTotal;
    private BookingPackageStatus status;
    private String externalPaymentId;
    private BigDecimal pricePaid;
    private Boolean enabled;
}
