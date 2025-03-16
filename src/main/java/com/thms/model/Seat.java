package com.thms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "seats")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "theatre_id", nullable = false)
    private Theatre theatre;

    @NotNull
    private Integer screenNumber;

    @NotBlank
    private String rowName;

    @NotNull
    @PositiveOrZero
    private Integer seatNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    private SeatType seatType;

    @NotNull
    @PositiveOrZero
    private Double priceMultiplier;

    public enum SeatType {
        STANDARD, PREMIUM, VIP, ACCESSIBLE
    }
}