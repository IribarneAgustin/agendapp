package com.reservateya.api.repository.entity;

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

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "slot_time")
@Audited
@AuditTable(value="slot_time_audit")
public class SlotTimeEntity extends PersistentObject {

    @ManyToOne
    @JoinColumn(name = "offering_id", nullable = false)
    private OfferingEntity offeringEntity;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "price")
    private Double price;

    @Column(name = "capacity_available")
    private Integer capacityAvailable;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

}
