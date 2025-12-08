package com.reservalink.api.repository;

import com.reservalink.api.repository.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u.id FROM UserEntity u JOIN brandEntity b WHERE b.name = :brandName")
    String findUserIdByBrandName(@Param("brandName") String brandName);

    List<UserEntity> findBySubscriptionEntity_EnabledTrueAndSubscriptionEntity_ExpiredFalseAndSubscriptionEntity_ExpirationBetween(LocalDateTime startOf3DaysFuture, LocalDateTime endOf3DaysFuture);

    List<UserEntity> findBySubscriptionEntity_EnabledTrueAndSubscriptionEntity_ExpiredTrueAndSubscriptionEntity_ExpirationBetween(LocalDateTime startOf3DaysAgo, LocalDateTime endOf3DaysAgo);

    List<UserEntity> findBySubscriptionEntity_EnabledTrueAndSubscriptionEntity_ExpiredFalseAndSubscriptionEntity_ExpirationBefore(
            LocalDateTime dateTime);
}
