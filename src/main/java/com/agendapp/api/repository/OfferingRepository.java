package com.agendapp.api.repository;

import com.agendapp.api.entity.Offering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferingRepository extends JpaRepository<Offering, String> {
    List<Offering> findByUserIdAndEnabledTrue(String userId);
}
