//// src/main/java/com/thms/controller/api/PasswordResetController.java
//package com.thms.controller.api;
//
//import com.thms.dto.ApiResponse;
//import com.thms.dto.auth.PasswordResetRequest;
//import com.thms.dto.auth.PasswordResetTokenRequest;
//import com.thms.service.PasswordResetService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/auth/password")
//@Tag(name = "Password Reset", description = "API endpoints for handling password reset")
//public class PasswordResetController {
//
//    @Autowired
//    private PasswordResetService passwordResetService;
//
//    /**
//     * Initiate password reset by sending reset link to user's email
//     */
//    @PostMapping("/forgot")
//    @Operation(summary = "Forgot Password", description = "Initiate password reset and send email with reset link")
//    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
//        boolean emailSent = passwordResetService.initiatePasswordReset(request.getEmail());
//
//        // Always return success to prevent email enumeration attacks
//        return ResponseEntity.ok(ApiResponse.success(null,
//                "If an account with that email exists, a password reset link has been sent."));
//    }
//
//    /**
//     * Validate password reset token
//     */
//    @PostMapping("/validate-token")
//    @Operation(summary = "Validate Token", description = "Validate password reset token")
//    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(@Valid @RequestBody PasswordResetTokenRequest request) {
//        String email = passwordResetService.validateResetToken(request.getToken());
//
//        if (email == null) {
//            return ResponseEntity.badRequest()
//                    .body(ApiResponse.error("Invalid or expired token. Please request a new password reset link."));
//        }
//
//        Map<String, Object> response = new HashMap<>();
//        response.put("email", email);
//        response.put("token", request.getToken());
//
//        return ResponseEntity.ok(ApiResponse.success(response, "Valid reset token"));
//    }
//
//    /**
//     * Reset password using token
//     */
//    @PostMapping("/reset")
//    @Operation(summary = "Reset Password", description = "Reset password using token")
//    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetTokenRequest request) {
//        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
//            return ResponseEntity.badRequest()
//                    .body(ApiResponse.error("Password must be at least 6 characters long"));
//        }
//
//        boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());
//
//        if (!success) {
//            return ResponseEntity.badRequest()
//                    .body(ApiResponse.error("Failed to reset password. Invalid or expired token."));
//        }
//
//        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successful. You can now login with your new password."));
//    }
//}
// src/main/java/com/thms/controller/api/PasswordResetController.java
package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.dto.auth.PasswordResetRequest;
import com.thms.dto.auth.PasswordResetTokenRequest;
import com.thms.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/password")
@Tag(name = "Password Reset", description = "API endpoints for handling password reset")
public class PasswordResetController {

    @Autowired
    private PasswordResetService passwordResetService;

    /**
     * Initiate password reset by sending reset link to user's email
     */
    @PostMapping("/forgot")
    @Operation(summary = "Forgot Password", description = "Initiate password reset and send email with reset link")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody PasswordResetRequest request) {
        System.out.println("Password reset request for email: " + request.getEmail());

        boolean emailSent = passwordResetService.initiatePasswordReset(request.getEmail());

        System.out.println("Password reset email sent: " + emailSent);

        // Always return success to prevent email enumeration attacks
        return ResponseEntity.ok(ApiResponse.success(null,
                "If an account with that email exists, a password reset link has been sent."));
    }

    /**
     * Validate password reset token
     */
    @PostMapping("/validate-token")
    @Operation(summary = "Validate Token", description = "Validate password reset token")
    public ResponseEntity<ApiResponse<Map<String, Object>>> validateToken(@Valid @RequestBody PasswordResetTokenRequest request) {
        System.out.println("Validating reset token: " + request.getToken());

        String email = passwordResetService.validateResetToken(request.getToken());

        System.out.println("Token validation result - email: " + email);

        if (email == null) {
            System.out.println("Token validation failed - token is invalid or expired");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired token. Please request a new password reset link."));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("email", email);
        response.put("token", request.getToken());
        response.put("valid", true); // Add this field for frontend compatibility

        System.out.println("Token validation successful for email: " + email);

        return ResponseEntity.ok(ApiResponse.success(response, "Valid reset token"));
    }

    /**
     * Reset password using token
     */
    @PostMapping("/reset")
    @Operation(summary = "Reset Password", description = "Reset password using token")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody PasswordResetTokenRequest request) {
        System.out.println("Resetting password with token: " + request.getToken());

        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            System.out.println("Password reset failed - password too short");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Password must be at least 6 characters long"));
        }

        boolean success = passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

        System.out.println("Password reset result: " + success);

        if (!success) {
            System.out.println("Password reset failed - invalid or expired token");
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to reset password. Invalid or expired token."));
        }

        System.out.println("Password reset successful");
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successful. You can now login with your new password."));
    }
}