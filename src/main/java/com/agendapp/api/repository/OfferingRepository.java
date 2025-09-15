package com.agendapp.api.repository;

import com.agendapp.api.entity.Offering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OfferingRepository extends JpaRepository<Offering, String> {
    List<Offering> findByUserId(String userId);
}
