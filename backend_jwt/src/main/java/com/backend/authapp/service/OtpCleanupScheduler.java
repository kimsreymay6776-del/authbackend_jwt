package com.example.authapp.service;

import com.example.authapp.repository.OtpTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class OtpCleanupScheduler {

    private final OtpTokenRepository otpTokenRepository;

    // Runs every hour: purge OTP tokens that expired more than a day ago and were never consumed
    @Scheduled(cron = "0 0 * * * *")
    public void purgeExpiredOtps() {
        otpTokenRepository.deleteByExpiresAtBeforeAndConsumedFalse(LocalDateTime.now().minusDays(1));
    }
}
