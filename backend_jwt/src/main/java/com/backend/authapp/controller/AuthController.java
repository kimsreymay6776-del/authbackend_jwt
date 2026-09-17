package com.example.authapp.controller;

import com.example.authapp.dto.request.*;
import com.example.authapp.dto.response.ApiResponse;
import com.example.authapp.dto.response.AuthResponse;
import com.example.authapp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints backing the mobile screens:
 * Login -> Sign Up -> Forgot Password -> Verify OTP -> Reset New Password -> Reset Successfully
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Signup, login, and password recovery flow")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Create a new account (Sign Up screen)")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(@Valid @RequestBody SignupRequest request) {
        AuthResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Account created successfully", response));
    }

    @Operation(summary = "Authenticate a user (Login screen)")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Logged in successfully", response));
    }

    @Operation(summary = "Request an OTP code by email (Forgot Password screen)")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("An OTP code has been sent to your email"));
    }

    @Operation(summary = "Resend the OTP code (Verify OTP screen - 'Send code again')")
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOtp(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.resendOtp(request);
        return ResponseEntity.ok(ApiResponse.success("A new OTP code has been sent to your email"));
    }

    @Operation(summary = "Verify the 6-digit OTP code (Verify OTP screen)")
    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<Void>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP verified successfully"));
    }

    @Operation(summary = "Set a new password after OTP verification (Reset New Password screen)")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successfully"));
    }

    @Operation(summary = "Exchange a refresh token for a new access/refresh token pair")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }
}
