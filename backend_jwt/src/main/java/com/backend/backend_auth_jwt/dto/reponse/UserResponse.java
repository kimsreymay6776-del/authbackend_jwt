package com.backend.backend_auth_jwt.dto.reponse;

import com.backend.backend_auth_jwt.entity.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private boolean profileCreated;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .profileCreated(user.isProfileCreated())
                .build();
    }
}
