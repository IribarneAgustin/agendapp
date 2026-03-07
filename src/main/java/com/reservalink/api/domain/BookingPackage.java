package com.reservalink.api.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private BookingPackageStatus status;
    private String externalPaymentId;
}
