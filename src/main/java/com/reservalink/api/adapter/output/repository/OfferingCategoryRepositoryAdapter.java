package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.OfferingCategoryEntity;
import com.reservalink.api.adapter.output.repository.entity.UserEntity;
import com.reservalink.api.adapter.output.repository.mapper.OfferingCategoryMapper;
import com.reservalink.api.application.output.OfferingCategoryServiceRepositoryPort;
import com.reservalink.api.domain.OfferingCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OfferingCategoryRepositoryAdapter implements OfferingCategoryServiceRepositoryPort {

    private final OfferingCategoryJpaRepository jpaRepository;
    private final OfferingCategoryMapper mapper;

    @Override
    public List<OfferingCategory> findAllByUserId(String userId) {
        return jpaRepository.findAllByUserEntity_IdAndEnabledTrue(userId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public OfferingCategory save(OfferingCategory category) {
        OfferingCategoryEntity entity = OfferingCategoryEntity.builder()
                .id(category.getId())
                .userEntity(UserEntity.builder().id(category.getUserId()).build())
                .name(category.getName())
                .enabled(category.getEnabled() != null ? category.getEnabled() : true)
                .isDefault(category.getIsDefault() != null ? category.getIsDefault() : false)
                .build();
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<OfferingCategory> findById(String categoryId) {
        return Optional.of(jpaRepository.findByIdAndEnabledTrue(categoryId)).map(mapper::toDomain);
    }

    @Override
    public Optional<OfferingCategory> findByUserIdAndIsDefault(String userId) {
        return Optional.ofNullable(jpaRepository.findByUserEntity_IdAndIsDefaultTrueAndEnabledTrue(userId)).map(mapper::toDomain);
    }

    @Override
    public Optional<OfferingCategory> findByUserIdAndName(String userId, String name) {
        return Optional.ofNullable(jpaRepository.findByUserEntity_IdAndNameAndEnabledTrue(userId, name)).map(mapper::toDomain);
    }

}