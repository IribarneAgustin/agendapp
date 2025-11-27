package com.reservalink.api.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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


@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "offering")
@Audited
@AuditTable(value="offering_audit")
public class OfferingEntity extends PersistentObject {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @Column(name = "name")
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "advance_payment_percentage")
    private Integer advancePaymentPercentage;

    @Column(name = "status")
    private Boolean status;

}
