package com.example.authapp.service;

import com.example.authapp.dto.request.*;
import com.example.authapp.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void verifyOtp(VerifyOtpRequest request);

    void resetPassword(ResetPasswordRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void resendOtp(ForgotPasswordRequest request);
}
