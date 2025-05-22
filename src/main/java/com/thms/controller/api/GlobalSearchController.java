// GlobalSearchController.java
package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.service.GlobalSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/search")
@Tag(name = "Global Search", description = "Global search endpoints")
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;

    public GlobalSearchController(GlobalSearchService globalSearchService) {
        this.globalSearchService = globalSearchService;
    }

    /**
     * Global search across all entities
     */
    @GetMapping
    @Operation(summary = "Global search", description = "Search across movies, theatres, screenings, and users")
    public ResponseEntity<ApiResponse<Map<String, Object>>> globalSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "3") int limit) {
        
        Map<String, Object> results = globalSearchService.globalSearch(query, limit);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * Search movies only
     */
    @GetMapping("/movies")
    @Operation(summary = "Search movies", description = "Search movies by title, genre, director, or cast")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchMovies(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> results = globalSearchService.searchMovies(query, limit);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * Search theatres only
     */
    @GetMapping("/theatres")
    @Operation(summary = "Search theatres", description = "Search theatres by name or address")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchTheatres(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> results = globalSearchService.searchTheatres(query, limit);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * Search screenings only
     */
    @GetMapping("/screenings")
    @Operation(summary = "Search screenings", description = "Search screenings by movie title or theatre name")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchScreenings(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> results = globalSearchService.searchScreenings(query, limit);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * Search users only (Admin only)
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search users", description = "Search users by username, email, or full name (Admin only)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchUsers(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> results = globalSearchService.searchUsers(query, limit);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    /**
     * Get search suggestions
     */
    @GetMapping("/suggestions")
    @Operation(summary = "Get search suggestions", description = "Get search suggestions based on query")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSearchSuggestions(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int limit) {
        
        Map<String, Object> suggestions = globalSearchService.getSearchSuggestions(query, limit);
        return ResponseEntity.ok(ApiResponse.success(suggestions));
    }
}
