package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.dto.TheatreDTO;
import com.thms.exception.ResourceNotFoundException;
import com.thms.service.TheatreService;
import com.thms.service.ScreeningService;
import com.thms.service.SeatService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/theatres")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminTheatreRestController {

    private final TheatreService theatreService;
    private final ScreeningService screeningService;
    private final SeatService seatService;

    public AdminTheatreRestController(
            TheatreService theatreService, 
            ScreeningService screeningService,
            SeatService seatService) {
        this.theatreService = theatreService;
        this.screeningService = screeningService;
        this.seatService = seatService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TheatreDTO>>> getTheatres(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "id") String sortBy) {
        
        List<TheatreDTO> theatres;
        
        // Handle search functionality
        if (search != null && !search.isEmpty()) {
            theatres = theatreService.searchTheatresByName(search);
        } else {
            theatres = theatreService.getAllTheatres();
        }
        
        return ResponseEntity.ok(ApiResponse.success(theatres));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TheatreDTO>> createTheatre(@Valid @RequestBody TheatreDTO theatreDTO) {
        try {
            TheatreDTO createdTheatre = theatreService.createTheatre(theatreDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdTheatre, "Theatre created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error creating theatre: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TheatreDTO>> getTheatre(@PathVariable("id") Long id) {
        TheatreDTO theatre = theatreService.getTheatreById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre", "id", id));
        
        return ResponseEntity.ok(ApiResponse.success(theatre));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TheatreDTO>> updateTheatre(
            @PathVariable("id") Long id,
            @Valid @RequestBody TheatreDTO theatreDTO) {
        
        try {
            // Check if theatre exists
            theatreService.getTheatreById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Theatre", "id", id));
            
            theatreDTO.setId(id);
            TheatreDTO updatedTheatre = theatreService.updateTheatre(id, theatreDTO)
                    .orElseThrow(() -> new ResourceNotFoundException("Theatre", "id", id));
            
            return ResponseEntity.ok(ApiResponse.success(updatedTheatre, "Theatre updated successfully"));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error updating theatre: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteTheatre(@PathVariable("id") Long id) {
        try {
            // Check if theatre exists
            theatreService.getTheatreById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Theatre", "id", id));
            
            theatreService.deleteTheatre(id);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Theatre deleted successfully"));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error deleting theatre: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/seats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTheatreSeats(@PathVariable("id") Long id) {
        TheatreDTO theatre = theatreService.getTheatreById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre", "id", id));
        
        Map<String, Object> response = new HashMap<>();
        response.put("theatre", theatre);
        
        // For each screen, get the seats
        Map<Integer, Object> screenSeats = new HashMap<>();
        for (int i = 1; i <= theatre.getTotalScreens(); i++) {
            screenSeats.put(i, seatService.getSeatsByTheatreAndScreen(id, i));
        }
        response.put("seats", screenSeats);
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    @PostMapping("/{id}/seats/initialize")
    public ResponseEntity<ApiResponse<Void>> initializeSeats(
            @PathVariable("id") Long id,
            @RequestParam("screenNumber") Integer screenNumber,
            @RequestParam("rows") Integer rows,
            @RequestParam("seatsPerRow") Integer seatsPerRow) {
        
        try {
            // Check if theatre exists
            theatreService.getTheatreById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Theatre", "id", id));
            
            seatService.initializeSeatsForTheatre(id, screenNumber, rows, seatsPerRow);
            
            return ResponseEntity.ok(ApiResponse.success(null, 
                    "Seats initialized for Screen " + screenNumber + " successfully"));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error initializing seats: " + e.getMessage()));
        }
    }
    
    @GetMapping("/{id}/screenings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTheatreScreenings(@PathVariable("id") Long id) {
        TheatreDTO theatre = theatreService.getTheatreById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre", "id", id));
        
        Map<String, Object> response = new HashMap<>();
        response.put("theatre", theatre);
        response.put("screenings", screeningService.getScreeningsByTheatre(id));
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
} 