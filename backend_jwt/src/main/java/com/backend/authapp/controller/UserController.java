package com.example.authapp.controller;

import com.example.authapp.dto.response.ApiResponse;
import com.example.authapp.dto.response.UserResponse;
import com.example.authapp.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example JWT-protected endpoint. Requires a valid "Authorization: Bearer <token>" header.
 */
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Authenticated user profile endpoints")
public class UserController {

    @Operation(summary = "Get the currently authenticated user's profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", UserResponse.fromEntity(user)));
    }
}
