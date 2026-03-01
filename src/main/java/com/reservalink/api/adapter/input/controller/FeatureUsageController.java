package com.reservalink.api.adapter.input.controller;

import com.reservalink.api.adapter.input.controller.request.FeatureUsageRequest;
import com.reservalink.api.application.dto.FeatureUsageResponse;
import com.reservalink.api.application.dto.FeatureUsageDetail;
import com.reservalink.api.application.service.feature.FeatureUsageService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user/{userId}/subscription-feature")
@Slf4j
public class FeatureUsageController {

    private final FeatureUsageService featureUsageService;

    public FeatureUsageController(FeatureUsageService featureUsageService) {
        this.featureUsageService = featureUsageService;
    }

    @PostMapping
    public ResponseEntity<FeatureUsageResponse> create(@PathVariable String userId,
                                               @RequestBody @Valid FeatureUsageRequest request) {
        log.info("Request to add new feature usage received for the userId {}", userId);
        FeatureUsageResponse created = featureUsageService.create(request, userId);
        log.info("Feature usage added successfully");
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(created);
    }


    @DeleteMapping("/{featureUsageId}")
    public ResponseEntity<Void> delete(@PathVariable String userId, @PathVariable String featureUsageId) {
        featureUsageService.delete(featureUsageId, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<FeatureUsageDetail>> getUserFeatures(@PathVariable String userId) {
        List<FeatureUsageDetail> features = featureUsageService.findAllAvailableByUserId(userId);
        return ResponseEntity.ok(features);
    }
}