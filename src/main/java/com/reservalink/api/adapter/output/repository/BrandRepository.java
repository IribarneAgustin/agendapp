package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.BrandEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<BrandEntity, String> {

    boolean existsByName(String name);
}
