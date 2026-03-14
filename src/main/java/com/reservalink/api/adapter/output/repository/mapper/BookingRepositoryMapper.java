package com.reservalink.api.adapter.output.repository.mapper;

import com.reservalink.api.adapter.output.repository.entity.BookingEntity;
import com.reservalink.api.domain.Booking;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BookingRepositoryMapper {

    private final SlotTimeRepositoryMapper slotTimeMapper;

    public Booking toDomain(BookingEntity entity) {
        if (entity == null) {
            return null;
        }
        return Booking.builder()
                .id(entity.getId())
                .slotTime(slotTimeMapper.toDomain(entity.getSlotTimeEntity()))
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .name(entity.getName())
                .quantity(entity.getQuantity())
                .status(entity.getStatus())
                .build();
    }
}