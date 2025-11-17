package com.agendapp.api.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "payment_account_token")
@Audited
@AuditTable(value = "payment_account_token_audit")
public class PaymentAccountTokenEntity extends PersistentObject {

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 50)
    private PaymentMethod paymentMethod;

    @Column(name = "access_token", nullable = false, length = 512)
    private String accessToken;

    @Column(name = "refresh_token", nullable = true, length = 512)
    private String refreshToken;

    @Column(name = "expires_at", nullable = true)
    private Long expiresAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;
}
