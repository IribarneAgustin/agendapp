package com.agendapp.api.repository;

import com.agendapp.api.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, String> {
    List<Subscription> findByEnabledTrueAndExpiredFalseAndExpirationLessThan(LocalDateTime now);
}
