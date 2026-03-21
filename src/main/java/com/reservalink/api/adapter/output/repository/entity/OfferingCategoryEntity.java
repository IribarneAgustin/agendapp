package com.reservalink.api.adapter.output.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "offering_category",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_offering_categories_user_name_enabled",
                        columnNames = {"user_id", "name", "enabled"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class OfferingCategoryEntity extends PersistentObject {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity userEntity;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "is_default")
    private Boolean isDefault;
}