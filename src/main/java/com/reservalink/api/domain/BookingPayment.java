package com.reservalink.api.domain;

import com.reservalink.api.domain.enums.PaymentMethod;
import com.reservalink.api.domain.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingPayment {
    private BigDecimal amount;
    private LocalDateTime paymentDate;
    private PaymentMethod paymentMethod;
    private String externalId;
    private PaymentStatus paymentStatus;
    private boolean enabled;
    private String bookingId;
}