package com.reservalink.api.application.service.offering;

import com.reservalink.api.domain.OfferingCategory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface OfferingCategoryService {
    List<OfferingCategory> getCategoriesByUserId(String userId);

    OfferingCategory create(OfferingCategory offeringCategory);

    @Transactional(rollbackFor = Exception.class)
    void deleteCategory(String categoryId);

    OfferingCategory update(OfferingCategory categoryRequest);

    List<OfferingCategory> getCategoriesInUseByUserId(String userId);
}
