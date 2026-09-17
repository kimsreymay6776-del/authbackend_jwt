package com.example.authapp.repository;

import com.example.authapp.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByEmailOrderByCreatedAtDesc(String email);

    void deleteByExpiresAtBeforeAndConsumedFalse(LocalDateTime cutoff);
}
