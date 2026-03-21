package com.reservalink.api.adapter.output.repository.mapper;

import com.reservalink.api.adapter.output.repository.entity.OfferingCategoryEntity;
import com.reservalink.api.domain.OfferingCategory;
import org.springframework.stereotype.Component;

@Component
public class OfferingCategoryMapper {

    public OfferingCategory toDomain(OfferingCategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return OfferingCategory.builder()
                .id(entity.getId())
                .userId(entity.getUserEntity().getId())
                .name(entity.getName())
                .isDefault(entity.getIsDefault())
                .build();
    }

}
