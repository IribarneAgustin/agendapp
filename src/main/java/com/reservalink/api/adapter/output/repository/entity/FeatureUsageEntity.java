package com.reservalink.api.adapter.output.repository.entity;

import com.reservalink.api.domain.FeatureStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "feature_usage")
public class FeatureUsageEntity extends PersistentObject {

    @ManyToOne
    @JoinColumn(name = "subscription_id", nullable = false)
    private SubscriptionEntity subscriptionEntity;

    @ManyToOne
    @JoinColumn(name = "subscription_feature_id", nullable = false)
    private SubscriptionFeatureEntity subscriptionFeatureEntity;

    @Column(name = "usage_count")
    private Integer usage;

    @Column(name = "feature_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private FeatureStatus featureStatus;
}