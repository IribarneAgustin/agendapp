package com.reservalink.api.application.output;

import com.reservalink.api.adapter.output.repository.entity.ResourceEntity;
import com.reservalink.api.domain.Resource;

import java.util.List;
import java.util.Optional;

public interface ResourceRepositoryPort {

    Resource create(Resource resource);

    Resource update(Resource resource);

    Optional<ResourceEntity> findById(String resourceId);

    void delete(String resourceId);

    List<Resource> findAllByUserId(String userId);

    Resource findResourceDomainById(String resourceId);

    List<Resource> findAllByUserIdAndOfferingId(String userId, String offeringId);
}