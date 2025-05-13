package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.dto.MovieDTO;
import com.thms.exception.ResourceNotFoundException;
import com.thms.model.Movie;
import com.thms.service.MovieService;
import com.thms.service.ScreeningService;
import com.thms.service.TheatreService;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<List<MovieDTO>>> getMovies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false, defaultValue = "id") String sortBy) {
        
        List<MovieDTO> movies;
        
        // Handle search and filtering
        if (search != null && !search.isEmpty()) {
            movies = movieService.searchMoviesByTitle(search);
        } else if (genre != null && !genre.isEmpty()) {
            try {
                Movie.Genre genreEnum = Movie.Genre.valueOf(genre);
                movies = movieService.getMoviesByGenre(genreEnum);
            } catch (IllegalArgumentException e) {
                movies = movieService.getAllMovies();
            }
        } else {
            movies = movieService.getAllMovies();
        }
        
        return ResponseEntity.ok(ApiResponse.success(movies));
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