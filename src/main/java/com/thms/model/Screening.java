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
@Table(name = "screenings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Screening {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "theatre_id", nullable = false)
    private Theatre theatre;

    @NotNull
    private LocalDateTime startTime;

    @NotNull
    private LocalDateTime endTime;

    @NotNull
    private Integer screenNumber;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ScreeningFormat format;

    @NotNull
    @PositiveOrZero
    private Double basePrice;

    @OneToMany(mappedBy = "screening", cascade = CascadeType.ALL)
    private Set<Booking> bookings = new HashSet<>();

    public enum ScreeningFormat {
        STANDARD, IMAX, DOLBY_ATMOS, THREE_D, FOUR_D
    }
}