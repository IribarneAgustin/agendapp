package com.reservalink.api.adapter.output.repository.entity;

import com.reservalink.api.domain.BookingPackageStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "booking_package")
public class BookingPackageEntity extends PersistentObject {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_session_id")
    private PackageSessionEntity packageSession;

    @Column(name = "sessions_total", nullable = false)
    private Integer sessionsTotal;

    @Column(name = "sessions_used", nullable = false)
    private Integer sessionsUsed;

    @Column(name = "price_paid", nullable = false)
    private BigDecimal pricePaid;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingPackageStatus status;

    @Column(name = "external_payment_id")
    private String externalPaymentId;
}