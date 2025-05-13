package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.dto.MovieDTO;
import com.thms.dto.ScreeningDTO;
import com.thms.exception.ResourceNotFoundException;
import com.thms.model.Movie;
import com.thms.service.MovieService;
import com.thms.service.ScreeningService;
import com.thms.service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/movies")
public class MovieRestController {

    private final MovieService movieService;
    private final TheatreService theatreService;
    private final ScreeningService screeningService;

    public MovieRestController(MovieService movieService, TheatreService theatreService, ScreeningService screeningService) {
        this.movieService = movieService;
        this.theatreService = theatreService;
        this.screeningService = screeningService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MovieDTO>>> getAllMovies(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        List<MovieDTO> movies;

        // Get movies based on filters
        if (query != null && !query.isEmpty()) {
            movies = movieService.searchMoviesByTitle(query);
        } else if (genre != null && !genre.isEmpty()) {
            try {
                Movie.Genre movieGenre = Movie.Genre.valueOf(genre.toUpperCase());
                movies = movieService.getMoviesByGenre(movieGenre);
            } catch (IllegalArgumentException e) {
                movies = movieService.getAllMovies();
            }
        } else {
            movies = movieService.getAllMovies();
        }

        return ResponseEntity.ok(ApiResponse.success(movies));
    }

    @GetMapping("/screenings")
    public ResponseEntity<ApiResponse<Map<Long, List<ScreeningDTO>>>> getMovieScreenings(
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        // Get screenings for today or specified date
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;

        if (date != null) {
            startDateTime = LocalDateTime.of(date, LocalTime.MIDNIGHT);
            endDateTime = LocalDateTime.of(date, LocalTime.MAX);
        } else {
            // Default to today
            LocalDate today = LocalDate.now();
            startDateTime = LocalDateTime.of(today, LocalTime.MIDNIGHT);
            endDateTime = LocalDateTime.of(today, LocalTime.MAX);
        }

        List<ScreeningDTO> screenings = screeningService.getScreeningsByDateRange(startDateTime, endDateTime);

        // Group screenings by movie ID
        Map<Long, List<ScreeningDTO>> screeningsByMovie = screenings.stream()
                .collect(Collectors.groupingBy(ScreeningDTO::getMovieId));

        return ResponseEntity.ok(ApiResponse.success(screeningsByMovie));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> getMovieById(@PathVariable("id") Long id) {
        MovieDTO movie = movieService.getMovieById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));

        return ResponseEntity.ok(ApiResponse.success(movie));
    }

    @GetMapping("/{id}/screenings")
    public ResponseEntity<ApiResponse<Map<LocalDate, List<ScreeningDTO>>>> getMovieScreenings(
            @PathVariable("id") Long id,
            @RequestParam(value = "days", defaultValue = "7") Integer days) {
        
        // Check if movie exists
        movieService.getMovieById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        
        // Get screenings for this movie (next X days)
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = startDateTime.plusDays(days);

        List<ScreeningDTO> screenings = screeningService.getScreeningsByMovie(id)
                .stream()
                .filter(s -> s.getStartTime().isAfter(startDateTime) && s.getStartTime().isBefore(endDateTime))
                .collect(Collectors.toList());

        // Group screenings by date
        Map<LocalDate, List<ScreeningDTO>> screeningsByDate = screenings.stream()
                .collect(Collectors.groupingBy(s -> s.getStartTime().toLocalDate()));

        return ResponseEntity.ok(ApiResponse.success(screeningsByDate));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<MovieDTO>>> searchMovies(@RequestParam(value = "query") String query) {
        List<MovieDTO> movies = movieService.searchMoviesByTitle(query);
        return ResponseEntity.ok(ApiResponse.success(movies));
    }

    @GetMapping("/genre/{genre}")
    public ResponseEntity<ApiResponse<List<MovieDTO>>> getMoviesByGenre(@PathVariable("genre") String genre) {
        try {
            Movie.Genre movieGenre = Movie.Genre.valueOf(genre.toUpperCase());
            List<MovieDTO> movies = movieService.getMoviesByGenre(movieGenre);
            return ResponseEntity.ok(ApiResponse.success(movies));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Invalid genre: " + genre));
        }
    }

    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<String>>> getAllGenres() {
        List<String> genres = Arrays.stream(Movie.Genre.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(genres));
    }

    @GetMapping("/ratings")
    public ResponseEntity<ApiResponse<List<String>>> getAllRatings() {
        List<String> ratings = Arrays.stream(Movie.Rating.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(ratings));
    }

    // Admin-only methods below

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping
    public ResponseEntity<ApiResponse<MovieDTO>> createMovie(@Valid @RequestBody MovieDTO movieDTO) {
        MovieDTO createdMovie = movieService.createMovie(movieDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdMovie, "Movie created successfully"));
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> updateMovie(
            @PathVariable("id") Long id,
            @Valid @RequestBody MovieDTO movieDTO) {
        
        // Check if movie exists
        movieService.getMovieById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        
        movieDTO.setId(id);
        MovieDTO updatedMovie = movieService.updateMovie(id, movieDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        
        return ResponseEntity.ok(ApiResponse.success(updatedMovie, "Movie updated successfully"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable("id") Long id) {
        // Check if movie exists
        movieService.getMovieById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));
        
        movieService.deleteMovie(id);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Movie deleted successfully"));
    }
} 