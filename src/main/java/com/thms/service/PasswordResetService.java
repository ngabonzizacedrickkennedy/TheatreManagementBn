// src/main/java/com/thms/service/PasswordResetService.java
package com.thms.service;

import com.thms.model.User;
import com.thms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final int TOKEN_VALIDITY_HOURS = 24;

    // Store reset tokens with expiration time (in production, use a database)
    private final Map<String, TokenData> tokenStore = new HashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Initiate password reset for a user
     *
     * @param email User's email address
     * @return True if reset token was generated and sent successfully
     */
    public boolean initiatePasswordReset(String email) {
        System.out.println("Initiating password reset for email: " + email);

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            System.out.println("User not found for email: " + email);
            return false; // User not found
        }

        User user = userOpt.get();
        String token = generateResetToken();
        LocalDateTime expiryTime = LocalDateTime.now().plusHours(TOKEN_VALIDITY_HOURS);

        // Store token with expiration time
        tokenStore.put(token, new TokenData(user.getEmail(), expiryTime));

        System.out.println("Generated reset token: " + token + " for email: " + email);
        System.out.println("Token expires at: " + expiryTime);
        System.out.println("Current tokens in store: " + tokenStore.keySet());

        // Send reset email
        boolean emailSent = sendResetEmail(user.getEmail(), token);
        System.out.println("Reset email sent: " + emailSent);

        return emailSent;
    }

    /**
     * Validate a password reset token
     *
     * @param token Reset token to validate
     * @return Email address associated with token if valid, null otherwise
     */
    public String validateResetToken(String token) {
        System.out.println("Validating reset token: " + token);
        System.out.println("Available tokens in store: " + tokenStore.keySet());

        TokenData tokenData = tokenStore.get(token);

        if (tokenData == null) {
            System.out.println("Token not found in store: " + token);
            return null; // Token not found
        }

        System.out.println("Found token data for email: " + tokenData.email);
        System.out.println("Token expiry time: " + tokenData.expiryTime);
        System.out.println("Current time: " + LocalDateTime.now());

        // Check if token is expired
        if (LocalDateTime.now().isAfter(tokenData.expiryTime)) {
            System.out.println("Token expired, removing from store");
            tokenStore.remove(token); // Clean up expired token
            return null;
        }

        System.out.println("Token is valid for email: " + tokenData.email);
        return tokenData.email;
    }

    /**
     * Complete password reset
     *
     * @param token       Reset token
     * @param newPassword New password
     * @return True if password was reset successfully
     */
    public boolean resetPassword(String token, String newPassword) {
        System.out.println("Attempting to reset password with token: " + token);

        String email = validateResetToken(token);

        if (email == null) {
            System.out.println("Invalid or expired token for password reset");
            return false; // Invalid or expired token
        }

        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            System.out.println("User not found for email during password reset: " + email);
            return false; // User not found
        }

        User user = userOpt.get();
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        System.out.println("Password updated successfully for user: " + email);

        // Remove used token
        tokenStore.remove(token);
        System.out.println("Used token removed from store");

        return true;
    }

    /**
     * Generate a secure random token for password reset
     *
     * @return Random UUID string
     */
    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    /**
     * Send password reset email
     *
     * @param email User's email address
     * @param token Reset token
     * @return True if email was sent successfully
     */
    private boolean sendResetEmail(String email, String token) {
        try {
            System.out.println("Attempting to send reset email to: " + email);

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Password Reset Request - Theatre Management System");
            message.setText("To reset your password, click on the link below:\n\n" +
                    "http://localhost:5173/reset-password?token=" + token + "\n\n" +
                    "This link will expire in " + TOKEN_VALIDITY_HOURS + " hours.\n\n" +
                    "If you didn't request a password reset, please ignore this email.");

            emailSender.send(message);
            System.out.println("Reset email sent successfully to: " + email);
            return true;
        } catch (Exception e) {
            System.err.println("Failed to send reset email to: " + email);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inner class to store token data with expiration time
     */
    private static class TokenData {
        private final String email;
        private final LocalDateTime expiryTime;

        public TokenData(String email, LocalDateTime expiryTime) {
            this.email = email;
            this.expiryTime = expiryTime;
        }
    }
}