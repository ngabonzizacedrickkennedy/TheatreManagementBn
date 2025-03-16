package com.thms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "bookings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "screening_id", nullable = false)
    private Screening screening;

    @NotNull
    private String bookingNumber;

    @NotNull
    private LocalDateTime bookingTime;

    @NotNull
    @PositiveOrZero
    private Double totalAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @ElementCollection
    @CollectionTable(name = "booking_seats", 
                     joinColumns = @JoinColumn(name = "booking_id"))
    private Set<String> bookedSeats = new HashSet<>();

    public enum PaymentStatus {
        PENDING, COMPLETED, CANCELLED, REFUNDED
    }
}