// src/main/java/com/thms/controller/api/TwoFactorAuthController.java
package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.dto.auth.LoginRequest;
import com.thms.dto.auth.LoginResponse;
import com.thms.dto.auth.OtpVerificationRequest;
import com.thms.model.User;
import com.thms.security.JwtTokenProvider;
import com.thms.service.TwoFactorAuthService;
import com.thms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/2fa")
@Tag(name = "Two-Factor Authentication", description = "API endpoints for handling two-factor authentication")
public class TwoFactorAuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider tokenProvider;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private TwoFactorAuthService twoFactorAuthService;
    
    /**
     * Initiate login with 2FA
     * First authenticate username and password, then send OTP
     */
    @PostMapping("/initiate")
    @Operation(summary = "Initiate 2FA login", description = "Validate credentials and send OTP code to user's email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> initiateLogin(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Step 1: Authenticate username and password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            
            // Step 2: Get user details
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            // Step 3: Generate and send OTP
            boolean otpSent = twoFactorAuthService.generateAndSendOtp(user);
            
            if (!otpSent) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Failed to send OTP. Please try again."));
            }
            
            // Step 4: Return partial success, indicating that 2FA is required
            Map<String, Object> response = new HashMap<>();
            response.put("requires2FA", true);
            response.put("username", user.getUsername());
            response.put("email", user.getEmail());
            
            return ResponseEntity.ok(ApiResponse.success(response, "OTP sent successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid username or password"));
        }
    }
    
    /**
     * Complete login by verifying OTP
     */
    @PostMapping("/verify")
    @Operation(summary = "Verify OTP", description = "Complete authentication by verifying the OTP code")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        // Step 1: Find user by username
        User user = userService.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Step 2: Verify OTP
        boolean isValid = twoFactorAuthService.verifyOtp(user.getEmail(), request.getOtp());
        
        if (!isValid) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid or expired OTP. Please try again."));
        }
        
        // Step 3: Generate authentication token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        LoginResponse loginResponse = new LoginResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities().toString());
        
        return ResponseEntity.ok(ApiResponse.success(loginResponse, "Login successful"));
    }
}

