package com.thms.service;

import com.thms.dto.ScreeningDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Service interface for screenings
 */
public interface IScreeningService {

    /**
     * Get all screenings with optional filtering
     */
    List<ScreeningDTO> getScreenings(Long movieId, Long theatreId, LocalDate date);

    /**
     * Get a screening by ID
     */
    Optional<ScreeningDTO> getScreeningById(Long id);

    /**
     * Get screenings for a movie
     */
    List<ScreeningDTO> getScreeningsByMovie(Long movieId, Integer days);

    /**
     * Get screenings for a theatre
     */
    List<ScreeningDTO> getScreeningsByTheatre(Long theatreId, LocalDate date);

    /**
     * Get upcoming screenings grouped by date
     */
    Map<String, List<ScreeningDTO>> getUpcomingScreenings(Integer days);

    /**
     * Get screenings by date range
     */
    List<ScreeningDTO> getScreeningsByDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * Get available seats for a screening
     */
    Set<String> getAvailableSeats(Long screeningId);

    /**
     * Get booked seats for a screening
     */
    Set<String> getBookedSeats(Long screeningId);

    /**
     * Get seating layout for a screening
     */
    Object getSeatingLayout(Long screeningId);
    Page<ScreeningDTO> getAllScreenings(Pageable pageable);
    Page<ScreeningDTO> getScreenings(Long movieId, Long theatreId, LocalDate date, Pageable pageable);
    Page<ScreeningDTO> getScreeningsByMovie(Long movieId, Pageable pageable);
    Page<ScreeningDTO> getScreeningsByTheatre(Long theatreId, Pageable pageable);
    Page<ScreeningDTO> getScreeningsByMovieAndTheatre(Long movieId, Long theatreId, Pageable pageable);
    Page<ScreeningDTO> getScreeningsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<ScreeningDTO> getAvailableScreenings(Long movieId, Long theatreId, LocalDateTime startDate, Pageable pageable);
    Page<ScreeningDTO> getUpcomingScreenings(LocalDateTime fromDateTime, Pageable pageable);
}