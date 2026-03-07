package com.reservalink.api.adapter.output.repository.entity;

import com.reservalink.api.domain.PackageSessionStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Audited
@Table(name = "package_session")
public class PackageSessionEntity extends PersistentObject {

    @OneToOne
    @JoinColumn(name = "offering_id", nullable = false)
    private OfferingEntity offeringEntity;

    @Column(name = "session_limit", nullable = false)
    private Integer sessionLimit;

    @Column(name = "price", nullable = false)
    private Double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PackageSessionStatus status;
}
