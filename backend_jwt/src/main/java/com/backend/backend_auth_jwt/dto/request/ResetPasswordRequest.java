package com.backend.backend_auth_jwt.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class ResetPasswordRequest {
    @NotBlank (message = "Email is required")
    @Email (message = "Email must be valid")
    private String email;

    @NotBlank (message = "New password is required")
    @Size (min = 8, message = "Password must be at lest 8 characters")
    private String newPassword;

    @NotBlank (message = "Comfirm password is required")
    private String comfirmPassword;
}
