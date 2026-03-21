package com.reservalink.api.application.output;

import com.reservalink.api.domain.Offering;

import java.util.List;
import java.util.Optional;

public interface OfferingRepositoryPort {
    List<Offering> findAllByCategoryId(String categoryId);

    void saveAll(List<Offering> offeringList);

    Offering save(Offering offering);

    Optional<Offering> findById(String id);

    List<Offering> findByUserId(String userId);

}
