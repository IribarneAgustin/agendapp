package com.reservalink.api.application.service.offering;

import com.reservalink.api.adapter.input.controller.request.CategoryRequest;
import com.reservalink.api.application.output.OfferingCategoryServiceRepositoryPort;
import com.reservalink.api.application.output.OfferingRepositoryPort;
import com.reservalink.api.domain.Offering;
import com.reservalink.api.domain.OfferingCategory;
import com.reservalink.api.exception.BusinessRuleException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.reservalink.api.exception.BusinessErrorCodes.DELETE_OFFERING_CATEGORY_DEFAULT;
import static com.reservalink.api.exception.BusinessErrorCodes.OFFERING_CATEGORY_ALREADY_EXISTS;

@Service
@Slf4j
@RequiredArgsConstructor
public class OfferingCategoryServiceImpl implements OfferingCategoryService {

    private final OfferingCategoryServiceRepositoryPort categoryRepository;
    private final OfferingRepositoryPort offeringRepository;

    @Override
    public List<OfferingCategory> getCategoriesByUserId(String userId) {
        return Optional.ofNullable(categoryRepository.findAllByUserId(userId)).orElse(Collections.emptyList()).stream()
                .filter(category -> !category.getIsDefault())
                .toList();
    }

    @Override
    public OfferingCategory create(OfferingCategory category) {
        log.info("Creating category {} for user {}", category.getName(), category.getUserId());
        boolean alreadyExist = categoryRepository.findByUserIdAndName(category.getUserId(),category.getName()).isPresent();
        if(alreadyExist) {
            throw new BusinessRuleException(OFFERING_CATEGORY_ALREADY_EXISTS.name());
        }
        category.setEnabled(true);
        category.setIsDefault(false);
        return categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(String categoryId) {
        OfferingCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryId));

        if (category.getIsDefault()) {
            throw new BusinessRuleException(DELETE_OFFERING_CATEGORY_DEFAULT.name());
        }

        OfferingCategory defaultCategory = categoryRepository.findByUserIdAndIsDefault(category.getUserId())
                .orElseThrow(() -> new IllegalStateException("Default category not found"));

        List<Offering> offerings = offeringRepository.findAllByCategoryId(categoryId);
        offerings.forEach(o -> o.setCategoryId(defaultCategory.getId()));
        offeringRepository.saveAll(offerings);

        category.setIsDefault(false);
        category.setEnabled(false);
        categoryRepository.save(category);
        log.info("Deleted category {} and reassigned {} offerings", categoryId, offerings.size());
    }

    @Override
    public OfferingCategory update(OfferingCategory categoryRequest) {
        OfferingCategory category = categoryRepository.findById(categoryRequest.getId())
                .orElseThrow(() -> new RuntimeException("Category not found"));

        if (Boolean.TRUE.equals(category.getIsDefault())) {
            throw new RuntimeException("Cannot modify default category");
        }

        category.setName(categoryRequest.getName().trim());

        return categoryRepository.save(category);
    }
}