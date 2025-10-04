package com.agendapp.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "slot_time")
@Audited
@AuditTable(value="slot_time_audit")
public class SlotTime {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(name = "id", updatable = false, nullable = false, length = 36)
    private String id;

    @ManyToOne
    @JoinColumn(name = "offering_id", nullable = false)
    private Offering offering;

    @Column(name = "start_date_time")
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time")
    private LocalDateTime endDateTime;

    @Column(name = "price")
    private Double price;

    @Column(name = "capacity_available")
    private Integer capacityAvailable;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "creation_user")
    private String creationUser;

    @Column(name = "modification_user")
    private String modificationUser;

    @Column(name = "creation_timestamp", updatable = false)
    private LocalDateTime creationTimestamp;

    @Column(name = "modification_timestamp")
    private LocalDateTime modificationTimestamp;

    @PrePersist
    public void prePersist() {
        creationTimestamp = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        modificationTimestamp = LocalDateTime.now();
    }

}
