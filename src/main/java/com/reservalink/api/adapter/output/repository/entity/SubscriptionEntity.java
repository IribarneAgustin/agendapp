package com.reservalink.api.adapter.output.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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

import static org.hibernate.envers.RelationTargetAuditMode.NOT_AUDITED;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Table(name = "subscription")
@Entity
@Audited
@AuditTable(value = "subscription_audit")
public class SubscriptionEntity extends PersistentObject {

    @Column(name = "expired")
    private boolean expired;

    @Column(name = "creation_date_time")
    private LocalDateTime creationDateTime;

    @Column(name = "expiration")
    private LocalDateTime expiration;

    @Column(name = "checkout_link")
    private String checkoutLink;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    @Audited(targetAuditMode = NOT_AUDITED)
    private SubscriptionPlanEntity subscriptionPlan;

    @Column(name = "selected_resources_limit")
    private Integer selectedResourcesLimit;
}
