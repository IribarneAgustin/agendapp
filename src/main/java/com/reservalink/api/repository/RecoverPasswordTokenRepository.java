package com.reservalink.api.repository;

import com.reservalink.api.repository.entity.RecoverPasswordTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RecoverPasswordTokenRepository extends JpaRepository<RecoverPasswordTokenEntity, String> {

    @Query("""
            SELECT t FROM RecoverPasswordTokenEntity t
            WHERE t.tokenHash = :tokenHash
              AND t.used = false
              AND t.expiration > :now
            """)
    Optional<RecoverPasswordTokenEntity> findValidToken(@Param("tokenHash") String tokenHash, @Param("now") LocalDateTime now);

}
