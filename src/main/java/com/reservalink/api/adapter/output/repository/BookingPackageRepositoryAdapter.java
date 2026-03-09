package com.reservalink.api.adapter.output.repository;

import com.reservalink.api.adapter.output.repository.entity.BookingPackageEntity;
import com.reservalink.api.adapter.output.repository.entity.PackageSessionEntity;
import com.reservalink.api.application.output.BookingPackageRepositoryPort;
import com.reservalink.api.domain.BookingPackage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class BookingPackageRepositoryAdapter implements BookingPackageRepositoryPort {

    private final BookingPackageJpaRepository jpaRepository;

    @Override
    public BookingPackage save(BookingPackage domain) {
        BookingPackageEntity entity = toEntity(domain);
        BookingPackageEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<BookingPackage> findById(String id) {
        return jpaRepository.findByIdAndEnabledTrue(id).map(this::toDomain);
    }

    private BookingPackageEntity toEntity(BookingPackage domain) {
        BookingPackageEntity entity = BookingPackageEntity.builder()
                .packageSession(PackageSessionEntity.builder().id(domain.getPackageSessionId()).build())
                .sessionsTotal(domain.getSessionsTotal())
                .sessionsUsed(domain.getSessionsUsed())
                .pricePaid(domain.getPricePaid())
                .customerEmail(domain.getCustomerEmail())
                .customerName(domain.getCustomerName())
                .customerPhone(domain.getCustomerPhone())
                .status(domain.getStatus())
                .externalPaymentId(domain.getExternalPaymentId())
                .build();
        entity.setId(domain.getId());
        entity.setEnabled(domain.getEnabled());
        return entity;
    }

    private BookingPackage toDomain(BookingPackageEntity entity) {
        return BookingPackage.builder()
                .id(entity.getId())
                .packageSessionId(entity.getPackageSession().getId())
                .sessionsTotal(entity.getSessionsTotal())
                .sessionsUsed(entity.getSessionsUsed())
                .pricePaid(entity.getPricePaid())
                .customerEmail(entity.getCustomerEmail())
                .customerName(entity.getCustomerName())
                .customerPhone(entity.getCustomerPhone())
                .status(entity.getStatus())
                .externalPaymentId(entity.getExternalPaymentId())
                .enabled(entity.getEnabled())
                .build();
    }
}