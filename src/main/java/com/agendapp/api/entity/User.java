package com.agendapp.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "user")
@Audited
@AuditTable(value="user_audit")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @ReadOnlyProperty
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

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
