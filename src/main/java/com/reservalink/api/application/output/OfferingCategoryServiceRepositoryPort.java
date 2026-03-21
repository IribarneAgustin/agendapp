package com.reservalink.api.application.output;

import com.reservalink.api.domain.OfferingCategory;

import java.util.List;
import java.util.Optional;

public interface OfferingCategoryServiceRepositoryPort {
    List<OfferingCategory> findAllByUserId(String userId);

    OfferingCategory save(OfferingCategory category);

    Optional<OfferingCategory> findById(String categoryId);

    Optional<OfferingCategory> findByUserIdAndIsDefault(String userId);

    Optional<OfferingCategory> findByUserIdAndName(String userId, String name);

}
