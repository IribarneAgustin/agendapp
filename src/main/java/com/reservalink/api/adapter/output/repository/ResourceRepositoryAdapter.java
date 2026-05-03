package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.ResourceEntity;
import com.reservalink.api.adapter.output.repository.entity.UserEntity;
import com.reservalink.api.application.output.ResourceRepositoryPort;
import com.reservalink.api.domain.Resource;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResourceRepositoryAdapter implements ResourceRepositoryPort {

    private final ResourceJpaRepository resourceJpaRepository;
    private final ModelMapper modelMapper;

    @Override
    public List<Resource> findAllByUserId(String userId) {
        return resourceJpaRepository.findAllByEnabledTrueAndUserEntity_Id(userId)
                .stream()
                .map(entity -> modelMapper.map(entity, Resource.class))
                .toList();
    }

    //FIXME When service layer be refactored, it must return domain entity
    @Override
    public Optional<ResourceEntity> findById(String resourceId) {
        return resourceJpaRepository.findById(resourceId);
    }

    @Override
    public Resource findResourceDomainById(String resourceId) {
        return resourceJpaRepository.findById(resourceId).map(e -> modelMapper.map(e, Resource.class))
                .orElseThrow(() -> new IllegalArgumentException("Resource id not found"));
    }

    @Override
    public Resource create(Resource resource) {
        ResourceEntity entity = modelMapper.map(resource, ResourceEntity.class);
        entity.setEnabled(true);
        UserEntity userRef = new UserEntity();
        userRef.setId(resource.getUserId());
        entity.setUserEntity(userRef);
        entity.setEmail(resource.getEmail());
        ResourceEntity saved = resourceJpaRepository.save(entity);
        return modelMapper.map(saved, Resource.class);
    }

    @Override
    public Resource update(Resource resource) {
        ResourceEntity entity = resourceJpaRepository.findById(resource.getId())
                .orElseThrow(() -> new IllegalArgumentException("Resource id not found"));
        entity.setName(resource.getName());
        entity.setLastName(resource.getLastName());
        entity.setIsDefault(resource.getIsDefault());
        entity.setEmail(resource.getEmail());
        return modelMapper.map(resourceJpaRepository.save(entity), Resource.class);
    }


    @Override
    public List<Resource> findAllByUserIdAndOfferingId(String userId, String offeringId) {
        return resourceJpaRepository.findAllByEnabledTrueAndUserIdAndOfferingId(userId, offeringId)
                .stream()
                .map(entity -> modelMapper.map(entity, Resource.class))
                .toList();
    }

    @Override
    public List<Resource> findAllBySubscriptionId(String subscriptionId) {
        return resourceJpaRepository.findAllByEnabledTrueAndUserEntity_SubscriptionEntity_Id(subscriptionId)
                .stream()
                .map(entity -> modelMapper.map(entity, Resource.class))
                .toList();
    }


    @Override
    public void delete(String resourceId) {
        ResourceEntity entity = resourceJpaRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource id not found"));
        entity.setEnabled(false);
        resourceJpaRepository.save(entity);
    }
}