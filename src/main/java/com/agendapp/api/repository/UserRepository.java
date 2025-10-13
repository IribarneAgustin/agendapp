package com.agendapp.api.repository;

import com.agendapp.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u.id FROM User u JOIN brand b WHERE b.name = :brandName")
    String findUserIdByBrandName(@Param("brandName") String brandName);

    List<User> findAllBySubscriptionIdIn(List<String> subscriptionIds);
}
