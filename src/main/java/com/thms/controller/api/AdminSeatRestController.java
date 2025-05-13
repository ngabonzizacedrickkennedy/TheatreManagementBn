package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.exception.ResourceNotFoundException;
import com.thms.model.Seat;
import com.thms.model.Theatre;
import com.thms.service.SeatService;
import com.thms.service.TheatreService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/seats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSeatRestController {

    private final SeatService seatService;
    private final TheatreService theatreService;

    public AdminSeatRestController(SeatService seatService, TheatreService theatreService) {
        this.seatService = seatService;
        this.theatreService = theatreService;
    }

    @GetMapping("/theatre/{theatreId}/screens")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTheatreScreens(@PathVariable("theatreId") Long theatreId) {
        Theatre theatre = theatreService.getTheatreEntityById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre", "id", theatreId));
        
        Map<String, Object> response = new HashMap<>();
        response.put("theatre", theatre);
        response.put("totalScreens", theatre.getTotalScreens());
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/theatre/{theatreId}/screen/{screenNumber}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSeatsByScreen(
            @PathVariable("theatreId") Long theatreId, 
            @PathVariable("screenNumber") Integer screenNumber) {
        
        Theatre theatre = theatreService.getTheatreEntityById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre", "id", theatreId));
        
        // Get seats grouped by row
        Map<String, List<Seat>> seatsByRow = seatService.getSeatMapByTheatreAndScreen(theatreId, screenNumber);
        
        Map<String, Object> response = new HashMap<>();
        response.put("theatre", theatre);
        response.put("screenNumber", screenNumber);
        response.put("seatsByRow", seatsByRow);
        response.put("seatTypes", Arrays.asList(Seat.SeatType.values()));
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/theatre/{theatreId}/screen/{screenNumber}/initialize")
    public ResponseEntity<ApiResponse<String>> initializeSeats(
            @PathVariable("theatreId") Long theatreId,
            @PathVariable("screenNumber") Integer screenNumber,
            @RequestParam("rows") Integer rows,
            @RequestParam("seatsPerRow") Integer seatsPerRow) {
        
        try {
            // Check if theatre exists
            theatreService.getTheatreEntityById(theatreId)
                    .orElseThrow(() -> new ResourceNotFoundException("Theatre", "id", theatreId));
            
            seatService.initializeSeatsForTheatre(theatreId, screenNumber, rows, seatsPerRow);
            
            return ResponseEntity.ok(ApiResponse.success(
                    null, 
                    String.format("Successfully initialized %d seats for screen %d", rows * seatsPerRow, screenNumber)));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error initializing seats: " + e.getMessage()));
        }
    }

    @PutMapping("/{seatId}")
    public ResponseEntity<ApiResponse<Void>> updateSeat(
            @PathVariable("seatId") Long seatId,
            @RequestParam("seatType") Seat.SeatType seatType,
            @RequestParam("priceMultiplier") Double priceMultiplier) {
        
        try {
            Seat seat = seatService.getSeatById(seatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Seat", "id", seatId));
            
            seatService.updateSeatType(seatId, seatType, priceMultiplier);
            
            return ResponseEntity.ok(ApiResponse.success(null, "Seat updated successfully"));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error updating seat: " + e.getMessage()));
        }
    }

    @PutMapping("/updateRow")
    public ResponseEntity<ApiResponse<Void>> updateSeatRow(
            @RequestParam("theatreId") Long theatreId,
            @RequestParam("screenNumber") Integer screenNumber,
            @RequestParam("rowName") String rowName,
            @RequestParam("seatType") Seat.SeatType seatType,
            @RequestParam("priceMultiplier") Double priceMultiplier) {
        
        try {
            // Check if theatre exists
            theatreService.getTheatreEntityById(theatreId)
                    .orElseThrow(() -> new ResourceNotFoundException("Theatre", "id", theatreId));
            
            seatService.updateSeatRowType(theatreId, screenNumber, rowName, seatType, priceMultiplier);
            
            return ResponseEntity.ok(ApiResponse.success(
                    null, String.format("Successfully updated seats in row %s", rowName)));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error updating row: " + e.getMessage()));
        }
    }

    @PutMapping("/bulkUpdate")
    public ResponseEntity<ApiResponse<String>> bulkUpdateSeats(
            @RequestParam("theatreId") Long theatreId,
            @RequestParam("screenNumber") Integer screenNumber,
            @RequestParam("selection") String selection,
            @RequestParam("seatType") Seat.SeatType seatType,
            @RequestParam("priceMultiplier") Double priceMultiplier) {
        
        try {
            // Check if theatre exists
            theatreService.getTheatreEntityById(theatreId)
                    .orElseThrow(() -> new ResourceNotFoundException("Theatre", "id", theatreId));
            
            String[] seatIds = selection.split(",");
            int updatedCount = seatService.bulkUpdateSeats(
                    Arrays.asList(seatIds), theatreId, screenNumber, seatType, priceMultiplier);
            
            return ResponseEntity.ok(ApiResponse.success(
                    null, String.format("Successfully updated %d seats", updatedCount)));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error updating seats: " + e.getMessage()));
        }
    }

    @DeleteMapping("/theatre/{theatreId}/screen/{screenNumber}")
    public ResponseEntity<ApiResponse<String>> deleteScreenSeats(
            @PathVariable("theatreId") Long theatreId,
            @PathVariable("screenNumber") Integer screenNumber) {
        
        try {
            // Check if theatre exists
            theatreService.getTheatreEntityById(theatreId)
                    .orElseThrow(() -> new ResourceNotFoundException("Theatre", "id", theatreId));
            
            int deletedCount = seatService.deleteScreenSeats(theatreId, screenNumber);
            
            return ResponseEntity.ok(ApiResponse.success(
                    null, String.format("Successfully deleted %d seats from screen %d", deletedCount, screenNumber)));
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Error deleting seats: " + e.getMessage()));
        }
    }
} 