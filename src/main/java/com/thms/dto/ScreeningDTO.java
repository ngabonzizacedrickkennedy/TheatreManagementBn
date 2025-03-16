package com.thms.dto;

import com.thms.model.Screening;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScreeningDTO {
    private Long id;
    
    @NotNull(message = "Movie is required")
    private Long movieId;
    private String movieTitle;
    
    @NotNull(message = "Theatre is required")
    private Long theatreId;
    private String theatreName;
    
    @NotNull(message = "Start time is required")
    @Future(message = "Start time must be in the future")
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    @NotNull(message = "Screen number is required")
    @Positive(message = "Screen number must be positive")
    private Integer screenNumber;
    
    @NotNull(message = "Format is required")
    private Screening.ScreeningFormat format;
    
    @NotNull(message = "Base price is required")
    @Positive(message = "Base price must be positive")
    private Double basePrice;
}