package com.agendapp.api.repository;

import com.agendapp.api.repository.entity.SubscriptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, String> {
    List<SubscriptionEntity> findByEnabledTrueAndExpiredFalseAndExpirationLessThan(LocalDateTime now);
}
