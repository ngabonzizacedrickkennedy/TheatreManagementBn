// src/main/java/com/thms/dto/auth/PasswordResetTokenRequest.java
package com.thms.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PasswordResetTokenRequest {
    
    @NotBlank(message = "Token is required")
    private String token;
    
    // Optional for validation, required for reset
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;
}