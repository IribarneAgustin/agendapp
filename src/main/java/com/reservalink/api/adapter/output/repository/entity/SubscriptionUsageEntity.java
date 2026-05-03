package com.reservalink.api.adapter.output.repository.entity;

import com.reservalink.api.domain.enums.PeriodStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "subscription_usage")
public class SubscriptionUsageEntity extends PersistentObject {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false, unique = true)
    private SubscriptionEntity subscription;

    @Column(name = "booking_usage")
    private Integer bookingUsage;

    @Column(name = "start_period_date_time", nullable = false)
    private LocalDateTime startPeriodDateTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_status", nullable = false)
    private PeriodStatus periodStatus;
}