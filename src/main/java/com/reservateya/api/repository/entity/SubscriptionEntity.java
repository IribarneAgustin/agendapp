package com.reservateya.api.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Table(name = "subscription")
@Entity
@Audited
@AuditTable(value="subscription_audit")
public class SubscriptionEntity extends PersistentObject {

    @Column(name = "expired")
    private Boolean expired;

    @Column(name = "creation_date_time")
    private LocalDateTime creationDateTime;

    @Column(name = "expiration")
    private LocalDateTime expiration;

    @Column(name = "checkout_link")
    private String checkoutLink;


}
