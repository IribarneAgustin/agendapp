package com.reservalink.api.adapter.output.repository.entity;

import com.reservalink.api.domain.enums.FeatureName;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;


@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "subscription_feature")
public class SubscriptionFeatureEntity extends PersistentObject {

    @Column(name = "name", nullable = false)
    @Enumerated(EnumType.STRING)
    private FeatureName name;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "price", nullable = false)
    private BigDecimal price;

}