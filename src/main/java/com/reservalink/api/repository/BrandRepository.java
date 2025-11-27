package com.reservalink.api.repository;

import com.reservalink.api.repository.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<BrandEntity, String> {

    boolean existsByName(String name);
}
