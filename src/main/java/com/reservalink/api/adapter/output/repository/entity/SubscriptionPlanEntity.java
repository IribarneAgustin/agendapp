package com.reservalink.api.adapter.output.repository.entity;

import com.reservalink.api.domain.enums.SubscriptionPlanCode;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "subscription_plan")
public class SubscriptionPlanEntity extends PersistentObject {

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, unique = true)
    private SubscriptionPlanCode code;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

    @Column(name = "max_bookings")
    private Integer maxBookings;

    @Column(name = "max_resources")
    private Integer maxResources;
}