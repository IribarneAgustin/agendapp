package com.reservalink.api.adapter.input.controller;

import com.reservalink.api.adapter.input.controller.request.CategoryRequest;
import com.reservalink.api.application.service.offering.OfferingCategoryService;
import com.reservalink.api.domain.OfferingCategory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/user/{userId}/category")
@RequiredArgsConstructor
@Slf4j
public class OfferingCategoryController {

    private final OfferingCategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<OfferingCategory>> getCategories(@PathVariable UUID userId) {
        List<OfferingCategory> categories = categoryService.getCategoriesByUserId(userId.toString());
        return ResponseEntity.ok(categories);
    }

    @PostMapping
    public ResponseEntity<OfferingCategory> createCategory(@PathVariable UUID userId, @RequestBody CategoryRequest request) {
        log.info("Creating new category for user: {}, name: {}", userId, request.name());
        OfferingCategory categoryRequest = OfferingCategory.builder()
                .userId(userId.toString())
                .name(request.name())
                .build();
        OfferingCategory newCategory = categoryService.create(categoryRequest);
        log.info("Offering category created successfully");
        return ResponseEntity.status(HttpStatus.CREATED).body(newCategory);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<OfferingCategory> updateCategory(@PathVariable UUID userId, @PathVariable UUID categoryId, @RequestBody CategoryRequest request) {
        log.info("Updating category: {} for user: {}", categoryId, userId);
        OfferingCategory categoryRequest = OfferingCategory.builder()
                .id(categoryId.toString())
                .userId(userId.toString())
                .name(request.name())
                .build();
        OfferingCategory updated = categoryService.update(categoryRequest);
        log.info("Category updated");
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID userId, @PathVariable UUID categoryId) {
        log.info("Deleting category: {} for user: {}", categoryId, userId);
        categoryService.deleteCategory(categoryId.toString());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/in-use")
    public ResponseEntity<List<OfferingCategory>> getCategoriesInUse(@PathVariable UUID userId) {
        List<OfferingCategory> categoriesInUse = categoryService.getCategoriesInUseByUserId(userId.toString());
        return ResponseEntity.ok(categoriesInUse);
    }
}