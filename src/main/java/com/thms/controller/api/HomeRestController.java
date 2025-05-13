package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.dto.MovieDTO;
import com.thms.dto.ScreeningDTO;
import com.thms.dto.ContactRequestDTO;
import com.thms.service.MovieService;
import com.thms.service.ScreeningService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class HomeRestController {

    private final MovieService movieService;
    private final ScreeningService screeningService;

    public HomeRestController(MovieService movieService, ScreeningService screeningService) {
        this.movieService = movieService;
        this.screeningService = screeningService;
    }

    @GetMapping("/home")
    public ResponseEntity<ApiResponse<String>> welcomePage() {
        return ResponseEntity.ok(ApiResponse.success("Welcome to Theatre Management System"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> home() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        
        Map<String, Object> response = new HashMap<>();
        
        if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_MANAGER")) {
            response.put("redirect", "/api/admin/dashboard");
            return ResponseEntity.ok(ApiResponse.success(response));
        }
        
        LocalDateTime now = LocalDateTime.now();
        List<ScreeningDTO> upcomingScreenings = screeningService.getUpcomingScreenings(now);

        // Group screenings by movie
        Map<Long, List<ScreeningDTO>> screeningsByMovie = upcomingScreenings.stream()
                .collect(Collectors.groupingBy(ScreeningDTO::getMovieId));

        // Get all current movies that have screenings
        List<MovieDTO> moviesWithScreenings = movieService.getMoviesByIds(screeningsByMovie.keySet());

        // Get upcoming movies (with future release dates)
        List<MovieDTO> upcomingMovies = movieService.getUpcomingMovies();

        response.put("nowPlaying", moviesWithScreenings);
        response.put("screeningsByMovie", screeningsByMovie);
        response.put("upcoming", upcomingMovies);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/about")
    public ResponseEntity<ApiResponse<Map<String, String>>> about() {
        Map<String, String> aboutInfo = new HashMap<>();
        aboutInfo.put("name", "Theatre Management System");
        aboutInfo.put("description", "A state-of-the-art system for managing theatre operations");
        aboutInfo.put("version", "1.0.0");
        
        return ResponseEntity.ok(ApiResponse.success(aboutInfo));
    }

    @GetMapping("/contact")
    public ResponseEntity<ApiResponse<String>> contact() {
        return ResponseEntity.ok(ApiResponse.success("Contact form is available"));
    }
    
    @PostMapping("/contact")
    public ResponseEntity<ApiResponse<String>> processContactForm(@Valid @RequestBody ContactRequestDTO contactRequest) {
        // In a real application, you would process the form data here
        // For example, send an email or save to database

        return ResponseEntity.ok(ApiResponse.success(null, "Thank you for your message! We'll get back to you shortly."));
    }
} 