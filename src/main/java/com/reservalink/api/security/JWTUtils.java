package com.reservalink.api.security;

import com.reservalink.api.repository.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;

@Component
public class JWTUtils {

    @Value("${jwt.secret.key}")
    private String secretKey;

    private SecretKey getSecretKey() {
        return new javax.crypto.spec.SecretKeySpec(secretKey.getBytes(), SignatureAlgorithm.HS256.getJcaName());
    }

    public String generateToken(UserEntity userDetails) {
        long expirationTime = 1000L * 60 * 60 * 24 * 7;
        return Jwts.builder()
                .claims()
                .subject(userDetails.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .and()
                .signWith(getSecretKey())
                .compact();

    }

    private Claims extractAllClaims(String token){
        return Jwts
                .parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getEmailFromToken(String token) {
        return Optional.ofNullable(extractAllClaims(token))
                .map(Claims::getSubject)
                .orElse(null);
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return Optional.ofNullable(extractAllClaims(token))
                .filter(claims -> claims.getSubject().equals(userDetails.getUsername()))
                .filter(claims -> !claims.getExpiration().before(new Date()))
                .isPresent();
    }


}
