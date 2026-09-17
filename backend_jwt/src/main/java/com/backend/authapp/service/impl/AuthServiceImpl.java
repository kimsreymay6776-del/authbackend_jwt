package com.example.authapp.service.impl;

import com.example.authapp.dto.request.*;
import com.example.authapp.dto.response.AuthResponse;
import com.example.authapp.dto.response.UserResponse;
import com.example.authapp.entity.OtpToken;
import com.example.authapp.entity.RefreshToken;
import com.example.authapp.entity.User;
import com.example.authapp.exception.*;
import com.example.authapp.repository.OtpTokenRepository;
import com.example.authapp.repository.RefreshTokenRepository;
import com.example.authapp.repository.UserRepository;
import com.example.authapp.security.JwtUtil;
import com.example.authapp.service.AuthService;
import com.example.authapp.service.EmailService;
import com.example.authapp.util.OtpGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final OtpTokenRepository otpTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Value("${app.otp.expiration-minutes}")
    private int otpExpirationMinutes;

    @Value("${app.otp.length}")
    private int otpLength;

    // ---------------- Sign Up ----------------

    @Override
    public AuthResponse signup(SignupRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Password and confirm password do not match");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        userRepository.save(user);

        return buildAuthResponse(user);
    }

    // ---------------- Login ----------------

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        return buildAuthResponse(user);
    }

    // ---------------- Forgot Password ----------------

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        String code = OtpGenerator.generate(otpLength);

        OtpToken otpToken = OtpToken.builder()
                .email(user.getEmail())
                .code(passwordEncoder.encode(code)) // store hashed, never plaintext
                .expiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .build();

        otpTokenRepository.save(otpToken);

        emailService.sendOtpEmail(user.getEmail(), code, otpExpirationMinutes);
    }

    @Override
    public void resendOtp(ForgotPasswordRequest request) {
        forgotPassword(request);
    }

    // ---------------- Verify OTP ----------------

    @Override
    public void verifyOtp(VerifyOtpRequest request) {
        OtpToken otpToken = otpTokenRepository.findTopByEmailOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(() -> new InvalidOtpException("No OTP request found for this email"));

        if (otpToken.isConsumed()) {
            throw new InvalidOtpException("This code has already been used. Please request a new one");
        }
        if (otpToken.isExpired()) {
            throw new InvalidOtpException("OTP code has expired. Please request a new one");
        }
        if (!passwordEncoder.matches(request.getCode(), otpToken.getCode())) {
            throw new InvalidOtpException("Invalid OTP code");
        }

        otpToken.setVerified(true);
        otpTokenRepository.save(otpToken);
    }

    // ---------------- Reset Password ----------------

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirm password do not match");
        }

        OtpToken otpToken = otpTokenRepository.findTopByEmailOrderByCreatedAtDesc(request.getEmail())
                .orElseThrow(() -> new InvalidOtpException("No verified OTP request found for this email"));

        if (!otpToken.isVerified() || otpToken.isConsumed()) {
            throw new InvalidOtpException("You must verify your OTP code before resetting your password");
        }
        if (otpToken.isExpired()) {
            throw new InvalidOtpException("OTP verification has expired. Please start again");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpToken.setConsumed(true);
        otpTokenRepository.save(otpToken);
    }

    // ---------------- Refresh Token ----------------

    @Override
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new InvalidTokenException("Invalid refresh token"));

        if (storedToken.isRevoked() || storedToken.isExpired()) {
            throw new InvalidTokenException("Refresh token has expired or been revoked");
        }

        User user = userRepository.findByEmail(storedToken.getUserEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Rotate: revoke old, issue new
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);

        return buildAuthResponse(user);
    }

    // ---------------- Helpers ----------------

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshTokenValue = jwtUtil.generateRefreshToken(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .userEmail(user.getEmail())
                .expiresAt(LocalDateTime.now().plusSeconds(jwtUtil.getRefreshTokenExpirationMs() / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenValue)
                .tokenType("Bearer")
                .user(UserResponse.fromEntity(user))
                .build();
    }
}
