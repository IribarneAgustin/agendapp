package com.reservalink.api.repository;

import com.reservalink.api.domain.Resource;
import com.reservalink.api.repository.entity.ResourceEntity;
import com.reservalink.api.repository.entity.UserEntity;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ResourceRepository {

    private final ResourceJpaRepository resourceJpaRepository;
    private final ModelMapper modelMapper;

    public ResourceRepository(ResourceJpaRepository resourceJpaRepository, ModelMapper modelMapper) {
        this.resourceJpaRepository = resourceJpaRepository;
        this.modelMapper = modelMapper;
    }


    public List<Resource> findAllByUserId(String userId) {
        return resourceJpaRepository.findAllByEnabledTrueAndUserEntity_Id(userId)
                .stream()
                .map(entity -> modelMapper.map(entity, Resource.class))
                .toList();
    }

    //FIXME When service layer be refactored, it must return domain entity
    public Optional<ResourceEntity> findById(String resourceId) {
        return resourceJpaRepository.findById(resourceId);
    }

    public Resource findResourceDomainById(String resourceId) {
        return resourceJpaRepository.findById(resourceId).map(e -> modelMapper.map(e, Resource.class))
                .orElseThrow(() -> new IllegalArgumentException("Resource id not found"));
    }

    public Resource create(Resource resource) {
        ResourceEntity entity = modelMapper.map(resource, ResourceEntity.class);
        entity.setEnabled(true);
        UserEntity userRef = new UserEntity();
        userRef.setId(resource.getUserId());
        entity.setUserEntity(userRef);

        ResourceEntity saved = resourceJpaRepository.save(entity);
        return modelMapper.map(saved, Resource.class);
    }

    public Resource update(Resource resource) {
        ResourceEntity entity = resourceJpaRepository.findById(resource.getId())
                .orElseThrow(() -> new IllegalArgumentException("Resource id not found"));
        entity.setName(resource.getName());
        entity.setLastName(resource.getLastName());
        entity.setIsDefault(resource.getIsDefault());
        return modelMapper.map(resourceJpaRepository.save(entity), Resource.class);
    }


    public List<Resource> findAllByUserIdAndOfferingId(String userId, String offeringId) {
        return resourceJpaRepository.findAllByEnabledTrueAndUserIdAndOfferingId(userId, offeringId)
                .stream()
                .map(entity -> modelMapper.map(entity, Resource.class))
                .toList();
    }


    public void delete(String resourceId) {
        ResourceEntity entity = resourceJpaRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource id not found"));
        entity.setEnabled(false);
        resourceJpaRepository.save(entity);
    }
}