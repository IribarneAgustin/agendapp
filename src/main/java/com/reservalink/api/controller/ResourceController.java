package com.reservalink.api.controller;

import com.reservalink.api.controller.request.ResourceRequest;
import com.reservalink.api.domain.Resource;
import com.reservalink.api.service.user.ResourceService;
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

@RestController
@RequestMapping("/resource")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping("/user/{userId}")
    ResponseEntity<List<Resource>> findAllByUserId(@PathVariable String userId) {
        List<Resource> resourceList = resourceService.findAllByUserId(userId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resourceList);
    }

    @GetMapping("/user/{userId}/offering/{offeringId}")
    ResponseEntity<List<Resource>> findAllByUserIdAndOfferingId(@PathVariable String userId, @PathVariable String offeringId) {
        List<Resource> resourceList = resourceService.findAllByUserIdAndOfferingId(userId, offeringId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resourceList);
    }

    @PostMapping("/user/{userId}")
    public ResponseEntity<Resource> create(@PathVariable String userId, @RequestBody ResourceRequest request) {
        Resource resource = Resource.builder()
                .name(request.name())
                .lastName(request.lastName())
                .isDefault(request.isDefault())
                .userId(userId)
                .build();
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceService.create(resource));
    }

    @PutMapping("/{resourceId}")
    public ResponseEntity<Resource> update(@PathVariable String resourceId, @RequestBody ResourceRequest request) {
        Resource resource = Resource.builder()
                .id(resourceId)
                .name(request.name())
                .lastName(request.lastName())
                .isDefault(request.isDefault())
                .build();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(resourceService.update(resource));
    }

    @DeleteMapping("/{resourceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String resourceId) {
        resourceService.delete(resourceId);
    }
}