package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.BookingPackageEntity;
import com.reservalink.api.application.output.BookingPackageRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BookingPackageRepositoryAdapter implements BookingPackageRepositoryPort {

    private final BookingPackageJpaRepository jpaRepository;

    @Override
    public BookingPackageEntity save(BookingPackageEntity entity) {
        return jpaRepository.save(entity);
    }

    @Override
    public Optional<BookingPackageEntity> findById(String id) {
        return jpaRepository.findById(id);
    }
}
