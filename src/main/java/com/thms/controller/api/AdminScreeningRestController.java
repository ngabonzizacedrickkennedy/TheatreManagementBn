package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.dto.BookingDTO;
import com.thms.dto.ScreeningDTO;
import com.thms.exception.ResourceNotFoundException;
import com.thms.model.Screening;
import com.thms.service.BookingService;
import com.thms.service.MovieService;
import com.thms.service.ScreeningService;
import com.thms.service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/screenings")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminScreeningRestController {

    private final ScreeningService screeningService;
    private final MovieService movieService;
    private final TheatreService theatreService;
    private final BookingService bookingService;

    public AdminScreeningRestController(
            ScreeningService screeningService,
            MovieService movieService,
            TheatreService theatreService,
            BookingService bookingService) {
        this.screeningService = screeningService;
        this.movieService = movieService;
        this.theatreService = theatreService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getScreenings(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long theatreId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {

        List<ScreeningDTO> screenings;

        // Apply filters if provided
        if (movieId != null && theatreId != null && date != null) {
            LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
            screenings = screeningService.getAvailableScreenings(movieId, theatreId, startOfDay);
        } else if (movieId != null && theatreId != null) {
            screenings = screeningService.getScreeningsByMovieAndTheatre(movieId, theatreId);
        } else if (movieId != null) {
            screenings = screeningService.getScreeningsByMovie(movieId);
        } else if (theatreId != null) {
            screenings = screeningService.getScreeningsByTheatre(theatreId);
        } else if (date != null) {
            LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
            screenings = screeningService.getScreeningsByDateRange(startOfDay, endOfDay);
        } else {
            screenings = screeningService.getAllScreenings();
        }

        Map<String, Object> response = new HashMap<>();
        response.put("screenings", screenings);
        response.put("movies", movieService.getAllMovies());
        response.put("theatres", theatreService.getAllTheatres());
        
        // Add filter values if provided
        if (movieId != null) response.put("selectedMovieId", movieId);
        if (theatreId != null) response.put("selectedTheatreId", theatreId);
        if (date != null) response.put("selectedDate", date);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/formats")
    public ResponseEntity<ApiResponse<List<String>>> getFormats() {
        List<String> formats = Arrays.stream(Screening.ScreeningFormat.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(formats));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getScreening(@PathVariable("id") Long id) {
        ScreeningDTO screening = screeningService.getScreeningById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", id));

        Map<String, Object> response = new HashMap<>();
        response.put("screening", screening);
        response.put("bookedSeats", bookingService.getBookedSeatsByScreeningId(id));
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ScreeningDTO>> createScreening(@Valid @RequestBody ScreeningDTO screeningDTO) {
        try {
            // Handle date/time conversion if needed
            if (screeningDTO.getStartDateString() != null && screeningDTO.getStartTimeString() != null) {
                LocalDateTime combinedDateTime = LocalDateTime.parse(
                        screeningDTO.getStartDateString() + "T" + screeningDTO.getStartTimeString() + ":00",
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                );
                screeningDTO.setStartTime(combinedDateTime);
            } else if (screeningDTO.getStartTime() == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Start date and time are required"));
            }

            // Create the screening
            ScreeningDTO createdScreening = screeningService.createScreening(screeningDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdScreening, "Screening created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error creating screening: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ScreeningDTO>> updateScreening(
            @PathVariable("id") Long id,
            @Valid @RequestBody ScreeningDTO screeningDTO) {

        try {
            // Check if screening exists
            screeningService.getScreeningById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", id));
            
            // Handle date/time conversion if needed
            if (screeningDTO.getStartDateString() != null && screeningDTO.getStartTimeString() != null) {
                LocalDateTime combinedDateTime = LocalDateTime.parse(
                        screeningDTO.getStartDateString() + "T" + screeningDTO.getStartTimeString() + ":00",
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                );
                screeningDTO.setStartTime(combinedDateTime);
            }
            
            screeningDTO.setId(id);
            ScreeningDTO updatedScreening = screeningService.updateScreening(id, screeningDTO)
                    .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", id));
            
            return ResponseEntity.ok(ApiResponse.success(updatedScreening, "Screening updated successfully"));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error updating screening: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteScreening(@PathVariable("id") Long id) {
        try {
            // Check if screening exists
            screeningService.getScreeningById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", id));
            
            screeningService.deleteScreening(id);
            return ResponseEntity.ok(ApiResponse.success(null, "Screening deleted successfully"));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error deleting screening: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}/bookings")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getScreeningBookings(@PathVariable("id") Long id) {
        // Check if screening exists
        screeningService.getScreeningById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Screening", "id", id));
        
        return ResponseEntity.ok(ApiResponse.success(bookingService.getBookingsByScreeningId(id)));
    }
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getScreenings(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long theatreId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            @RequestParam(required = false, defaultValue = "startTime") String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String sortOrder,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {

        // Validate page and size parameters
        if (page < 0) page = 0;
        if (size < 1) size = 10;
        if (size > 100) size = 100; // Limit maximum page size

        // Create sort direction
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ?
                Sort.Direction.DESC : Sort.Direction.ASC;

        // Validate sort field
        String validSortBy = validateSortField(sortBy);

        // Create pageable object
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, validSortBy));

        Page<ScreeningDTO> screeningPage = screeningService.getScreenings(movieId, theatreId, date, pageable);

        // Prepare response with pagination metadata
        Map<String, Object> response = new HashMap<>();
        response.put("screenings", screeningPage.getContent());
        response.put("currentPage", screeningPage.getNumber());
        response.put("totalPages", screeningPage.getTotalPages());
        response.put("totalElements", screeningPage.getTotalElements());
        response.put("pageSize", screeningPage.getSize());
        response.put("hasNext", screeningPage.hasNext());
        response.put("hasPrevious", screeningPage.hasPrevious());
        response.put("isFirst", screeningPage.isFirst());
        response.put("isLast", screeningPage.isLast());

        response.put("movies", movieService.getAllMovies());
        response.put("theatres", theatreService.getAllTheatres());

        // Add filter values if provided
        if (movieId != null) response.put("selectedMovieId", movieId);
        if (theatreId != null) response.put("selectedTheatreId", theatreId);
        if (date != null) response.put("selectedDate", date);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private String validateSortField(String sortBy) {
        // Define allowed sort fields to prevent SQL injection
        List<String> allowedFields = Arrays.asList(
                "startTime", "endTime", "screenNumber", "format", "basePrice", "id"
        );

        if (allowedFields.contains(sortBy)) {
            return sortBy;
        }

        return "startTime"; // Default fallback
    }
} 