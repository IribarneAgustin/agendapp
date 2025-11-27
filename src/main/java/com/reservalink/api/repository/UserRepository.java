package com.reservalink.api.repository;

import com.reservalink.api.repository.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u.id FROM UserEntity u JOIN brandEntity b WHERE b.name = :brandName")
    String findUserIdByBrandName(@Param("brandName") String brandName);

    List<UserEntity> findAllBySubscriptionEntityIdIn(List<String> subscriptionIds);
}
