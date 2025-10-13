package com.agendapp.api.repository;

import com.agendapp.api.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, String> {

    boolean existsByName(String name);
}
