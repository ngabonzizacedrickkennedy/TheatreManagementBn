package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.dto.ScreeningDTO;
import com.thms.exception.ResourceNotFoundException;
import com.thms.service.ScreeningService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Controller for public-facing screening endpoints
 */
@RestController
@RequestMapping("/api/screenings")
@Tag(name = "Screenings", description = "Public screening endpoints")
public class ScreeningController {
    
    @Autowired
    private ScreeningService screeningService;
    
    /**
     * Get all available screenings with optional filtering
     */
    @GetMapping
    @Operation(summary = "Get all screenings with optional filtering")
    public ResponseEntity<ApiResponse<List<ScreeningDTO>>> getScreenings(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long theatreId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<ScreeningDTO> screenings = screeningService.getScreenings(movieId, theatreId, date);
        return ResponseEntity.ok(ApiResponse.success(screenings));
    }
    
    /**
     * Get a screening by ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get screening details by ID")
    public ResponseEntity<ApiResponse<ScreeningDTO>> getScreeningById(@PathVariable Long id) {
        ScreeningDTO screening = screeningService.getScreeningById(id).orElseThrow(() -> new ResourceNotFoundException("Screening Id: "+id+" not found"));
        return ResponseEntity.ok(ApiResponse.success(screening));
    }
    
    /**
     * Get screenings for a specific movie
     */
    @GetMapping("/movie/{movieId}")
    @Operation(summary = "Get screenings for a specific movie")
    public ResponseEntity<ApiResponse<List<ScreeningDTO>>> getScreeningsByMovie(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "7") Integer days) {
        
        List<ScreeningDTO> screenings = screeningService.getScreeningsByMovie(movieId, days);
        return ResponseEntity.ok(ApiResponse.success(screenings));
    }
    
    /**
     * Get screenings for a specific theatre
     */
    @GetMapping("/theatre/{theatreId}")
    @Operation(summary = "Get screenings for a specific theatre") 
    public ResponseEntity<ApiResponse<List<ScreeningDTO>>> getScreeningsByTheatre(
            @PathVariable Long theatreId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        List<ScreeningDTO> screenings = screeningService.getScreeningsByTheatre(theatreId, date);
        return ResponseEntity.ok(ApiResponse.success(screenings));
    }
    
    /**
     * Get upcoming screenings grouped by date
     */
    @GetMapping("/upcoming")
    @Operation(summary = "Get upcoming screenings grouped by date")
    public ResponseEntity<ApiResponse<Map<String, List<ScreeningDTO>>>> getUpcomingScreenings(
            @RequestParam(defaultValue = "7") Integer days) {
        
        Map<String, List<ScreeningDTO>> screenings = screeningService.getUpcomingScreenings(days);
        return ResponseEntity.ok(ApiResponse.success(screenings));
    }
    
    /**
     * Get screenings by date range
     */
    @GetMapping("/date-range")
    @Operation(summary = "Get screenings within a date range")
    public ResponseEntity<ApiResponse<List<ScreeningDTO>>> getScreeningsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<ScreeningDTO> screenings = screeningService.getScreeningsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(screenings));
    }
    
    /**
     * Get available seats for a screening
     */
    @GetMapping("/{id}/seats")
    @Operation(summary = "Get available seats for a screening")
    public ResponseEntity<ApiResponse<Set<String>>> getAvailableSeats(@PathVariable Long id) {
        Set<String> availableSeats = screeningService.getAvailableSeats(id);
        return ResponseEntity.ok(ApiResponse.success(availableSeats));
    }
    
    /**
     * Get booked seats for a screening
     */
    @GetMapping("/{id}/booked-seats")
    @Operation(summary = "Get booked seats for a screening")
    public ResponseEntity<ApiResponse<Set<String>>> getBookedSeats(@PathVariable Long id) {
        Set<String> bookedSeats = screeningService.getBookedSeats(id);
        return ResponseEntity.ok(ApiResponse.success(bookedSeats));
    }
    
    /**
     * Get seating layout for a screening
     */
    @GetMapping("/{id}/layout")
    @Operation(summary = "Get seating layout for a screening")
    public ResponseEntity<ApiResponse<Object>> getSeatingLayout(@PathVariable Long id) {
        Object layout = screeningService.getSeatingLayout(id);
        return ResponseEntity.ok(ApiResponse.success(layout));
    }
}