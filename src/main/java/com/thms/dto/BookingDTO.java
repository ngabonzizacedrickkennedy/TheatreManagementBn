package com.thms.dto;

import com.thms.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {
    private Long id;
    private String bookingNumber;
    private Long userId;
    private String username;
    private String userEmail;
    private Long screeningId;
    private String movieTitle;
    private String movieUrl;
    private String theatreName;
    private LocalDateTime screeningTime;
    private LocalDateTime bookingTime;
    private Double totalAmount;
    private Booking.PaymentStatus paymentStatus;
    private Set<String> bookedSeats;
    private String paymentMethod;
}