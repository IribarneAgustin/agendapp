package com.agendapp.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import java.time.LocalDateTime;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;


@Getter
@Setter
@MappedSuperclass
@NoArgsConstructor
@SuperBuilder
public abstract class PersistentObject {

    @Column(name = "enabled")
    private Boolean enabled;

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
