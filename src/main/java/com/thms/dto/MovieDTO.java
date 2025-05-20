package com.thms.dto;

import com.thms.model.Movie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Duration is required")
    @PositiveOrZero(message = "Duration must be a positive number")
    private Integer durationMinutes;

    private Movie.Genre genre;

    @Size(max = 255, message = "Director name cannot exceed 255 characters")
    private String director;

    @Size(max = 255, message = "Cast info cannot exceed 255 characters")
    private String cast;

    private LocalDate releaseDate;

//    @Size(max = 255, message = "Poster URL cannot exceed 255 characters")
    private String posterImageUrl;

//    @Size(max = 255, message = "Trailer URL cannot exceed 255 characters")
    private String trailerUrl;

    private Movie.Rating rating;
}