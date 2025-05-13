package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.dto.BookingDTO;
import com.thms.exception.ResourceNotFoundException;
import com.thms.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingRestController {

    private final BookingService bookingService;

    public BookingRestController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getUserBookings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        List<BookingDTO> bookings = bookingService.getBookingsByUsername(username);
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingDTO>> getBookingById(@PathVariable("id") Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Check if the booking exists
        BookingDTO booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        
        // Check if the user is allowed to access this booking
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
        
        if (!isAdmin && !booking.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You don't have permission to access this booking"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<BookingDTO>> createBooking(@Valid @RequestBody BookingDTO bookingDTO) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Required parameters for the booking service
        Long screeningId = bookingDTO.getScreeningId();
        List<String> selectedSeats = new ArrayList<>(bookingDTO.getBookedSeats());
        String paymentMethod = bookingDTO.getPaymentMethod();
        
        if (screeningId == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Screening ID is required"));
        }
        
        if (selectedSeats == null || selectedSeats.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Selected seats are required"));
        }
        
        if (paymentMethod == null || paymentMethod.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Payment method is required"));
        }
        
        BookingDTO createdBooking = bookingService.createBooking(screeningId, username, selectedSeats, paymentMethod);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdBooking, "Booking created successfully"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(@PathVariable("id") Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Check if the booking exists
        BookingDTO booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        
        // Check if the user is allowed to cancel this booking
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN") || a.getAuthority().equals("ROLE_MANAGER"));
        
        if (!isAdmin && !booking.getUsername().equals(username)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("You don't have permission to cancel this booking"));
        }
        
        bookingService.deleteBooking(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Booking cancelled successfully"));
    }

    // Admin only endpoints
    @GetMapping("/admin/all")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    public ResponseEntity<ApiResponse<List<BookingDTO>>> getAllBookings() {
        List<BookingDTO> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(ApiResponse.success(bookings));
    }
} 