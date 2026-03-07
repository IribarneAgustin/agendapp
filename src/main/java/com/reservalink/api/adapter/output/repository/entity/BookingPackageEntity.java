package com.reservalink.api.adapter.output.repository.entity;

import com.reservalink.api.domain.BookingPackageStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Audited
@Table(name = "booking_package")
public class BookingPackageEntity extends PersistentObject {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_session_id", nullable = false)
    private PackageSessionEntity packageSession;

    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "sessions_used", nullable = false)
    private Integer sessionsUsed;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BookingPackageStatus status;

    @Column(name = "external_payment_id")
    private String externalPaymentId;
}
