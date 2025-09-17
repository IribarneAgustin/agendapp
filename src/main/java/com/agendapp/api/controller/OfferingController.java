package com.agendapp.api.controller;

import com.agendapp.api.controller.request.OfferingRequest;
import com.agendapp.api.controller.response.OfferingResponse;
import com.agendapp.api.service.OfferingService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users/{userId}/offerings")
@Slf4j
public class OfferingController {

    private final OfferingService offeringService;

    public OfferingController(OfferingService offeringService) {
        this.offeringService = offeringService;
    }

    @PostMapping
    public ResponseEntity<OfferingResponse> create(@Valid @RequestBody OfferingRequest offeringRequest) {
        OfferingResponse response = offeringService.create(offeringRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{offeringId}")
    public ResponseEntity<OfferingResponse> update(@PathVariable UUID offeringId, @Valid @RequestBody OfferingRequest offeringRequest) throws Exception {
        offeringRequest.setId(offeringId);
        OfferingResponse response = offeringService.update(offeringRequest);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<OfferingResponse>> findAll(@PathVariable UUID userId) {
        List<OfferingResponse> offerings = offeringService.findAllByUserId(userId);
        return ResponseEntity.ok(offerings);
    }

    @DeleteMapping("/{offeringId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID offeringId) {
        offeringService.delete(offeringId);
    }
}
