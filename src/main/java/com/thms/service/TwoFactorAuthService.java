// src/main/java/com/thms/service/TwoFactorAuthService.java
package com.thms.service;

import com.thms.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class TwoFactorAuthService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_VALIDITY_MINUTES = 10;

    // Store OTP codes with expiration time (in production, use Redis or another database)
    private final Map<String, OtpData> otpStore = new HashMap<>();

    @Autowired
    private JavaMailSender emailSender;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Generate and send an OTP code to the user's email
     * @param user The user requiring 2FA
     * @return True if OTP was generated and sent successfully
     */
    public boolean generateAndSendOtp(User user) {
        String otp = generateOtp();
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES);

        // Store OTP with expiration time
        otpStore.put(user.getEmail(), new OtpData(otp, expiryTime));

        // Send OTP via email
        return sendOtpEmail(user.getEmail(), otp);
    }

    /**
     * Verify the OTP provided by the user
     * @param email User's email
     * @param otpToVerify OTP provided by the user
     * @return True if OTP is valid and not expired
     */
    public boolean verifyOtp(String email, String otpToVerify) {
        OtpData otpData = otpStore.get(email);

        if (otpData == null) {
            return false; // No OTP found for this email
        }

        // Check if OTP is expired
        if (LocalDateTime.now().isAfter(otpData.expiryTime)) {
            otpStore.remove(email); // Clean up expired OTP
            return false;
        }

        // Verify OTP
        boolean isValid = otpData.otp.equals(otpToVerify);

        if (isValid) {
            otpStore.remove(email); // OTP can only be used once
        }

        return isValid;
    }

    /**
     * Generate a random OTP code
     * @return Random numeric OTP
     */
    private String generateOtp() {
        StringBuilder otp = new StringBuilder();
        SecureRandom random = new SecureRandom();

        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10)); // Generate random digit (0-9)
        }

        return otp.toString();
    }

    /**
     * Send OTP to user's email
     * @param email User's email address
     * @param otp OTP code to send
     * @return True if email was sent successfully
     */
    private boolean sendOtpEmail(String email, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Your Theatre Management System Verification Code");
            message.setText("Your verification code is: " + otp +
                    "\nThis code will expire in " + OTP_VALIDITY_MINUTES + " minutes.");

            emailSender.send(message);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inner class to store OTP data with expiration time
     */
    private static class OtpData {
        private final String otp;
        private final LocalDateTime expiryTime;

        public OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }
}
