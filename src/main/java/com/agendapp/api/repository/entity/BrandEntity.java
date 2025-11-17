package com.agendapp.api.repository.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "brand", uniqueConstraints = {
        @UniqueConstraint(name = "UK_BRAND_NAME", columnNames = "name")
})
@Audited
@AuditTable(value="brand_audit")
public class BrandEntity extends PersistentObject {

    @Column(name = "name")
    private String name;
}
