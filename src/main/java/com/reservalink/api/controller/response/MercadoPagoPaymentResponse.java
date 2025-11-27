package com.reservalink.api.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class MercadoPagoPaymentResponse {
    private Long id;
    private String status;
    private String status_detail;
    private BigDecimal transaction_amount;
    private Map<String, Object> metadata;
}
