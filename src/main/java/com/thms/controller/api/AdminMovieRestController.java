package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.dto.MovieDTO;
import com.thms.exception.ResourceNotFoundException;
import com.thms.model.Movie;
import com.thms.service.MovieService;
import com.thms.service.ScreeningService;
import com.thms.service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/admin/movies")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminMovieRestController {

    private final MovieService movieService;
    private final ScreeningService screeningService;
    private final TheatreService theatreService;

    public AdminMovieRestController(MovieService movieService,
                                    ScreeningService screeningService,
                                    TheatreService theatreService) {
        this.movieService = movieService;
        this.screeningService = screeningService;
        this.theatreService = theatreService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMovies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false, defaultValue = "title") String sortBy,
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

        Page<MovieDTO> moviePage;

        // Handle search and filtering with pagination
        if (search != null && !search.trim().isEmpty()) {
            moviePage = movieService.searchMoviesByTitle(search.trim(), pageable);
        } else if (genre != null && !genre.trim().isEmpty()) {
            try {
                Movie.Genre genreEnum = Movie.Genre.valueOf(genre.toUpperCase());
                moviePage = movieService.getMoviesByGenre(genreEnum, pageable);
            } catch (IllegalArgumentException e) {
                moviePage = movieService.getAllMovies(pageable);
            }
        } else {
            moviePage = movieService.getAllMovies(pageable);
        }

        // Prepare response with pagination metadata
        Map<String, Object> response = new HashMap<>();
        response.put("movies", moviePage.getContent());
        response.put("currentPage", moviePage.getNumber());
        response.put("totalPages", moviePage.getTotalPages());
        response.put("totalElements", moviePage.getTotalElements());
        response.put("pageSize", moviePage.getSize());
        response.put("hasNext", moviePage.hasNext());
        response.put("hasPrevious", moviePage.hasPrevious());
        response.put("isFirst", moviePage.isFirst());
        response.put("isLast", moviePage.isLast());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Validate and return a safe sort field
     */
    private String validateSortField(String sortBy) {
        // Define allowed sort fields to prevent SQL injection
        List<String> allowedFields = Arrays.asList(
                "title", "genre", "releaseDate", "rating", "durationMinutes", "director", "id"
        );

        if (allowedFields.contains(sortBy)) {
            return sortBy;
        }

        return "title"; // Default fallback
    }

    @GetMapping("/genres")
    public ResponseEntity<ApiResponse<List<String>>> getGenres() {
        List<String> genres = Arrays.stream(Movie.Genre.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(genres));
    }

    @GetMapping("/ratings")
    public ResponseEntity<ApiResponse<List<String>>> getRatings() {
        List<String> ratings = Arrays.stream(Movie.Rating.values())
                .map(Enum::name)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(ratings));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MovieDTO>> createMovie(@Valid @RequestBody MovieDTO movieDTO) {
        MovieDTO createdMovie = movieService.createMovie(movieDTO);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdMovie, "Movie created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MovieDTO>> getMovie(@PathVariable("id") Long id) {
        MovieDTO movie = movieService.getMovieById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));

        return ResponseEntity.ok(ApiResponse.success(movie));
    }

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

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteMovie(@PathVariable("id") Long id) {
        // Check if movie exists
        movieService.getMovieById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));

        movieService.deleteMovie(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Movie deleted successfully"));
    }

    @GetMapping("/{id}/screenings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMovieScreenings(@PathVariable("id") Long id) {
        MovieDTO movie = movieService.getMovieById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movie", "id", id));

        Map<String, Object> response = new HashMap<>();
        response.put("movie", movie);
        response.put("screenings", screeningService.getScreeningsByMovie(id));
        response.put("theatres", theatreService.getTheatresByMovie(id));

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}