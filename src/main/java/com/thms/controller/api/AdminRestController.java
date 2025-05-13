package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.dto.BookingDTO;
import com.thms.dto.MovieDTO;
import com.thms.dto.UserDTO;
import com.thms.exception.ResourceNotFoundException;
import com.thms.model.Booking;
import com.thms.service.BookingService;
import com.thms.service.MovieService;
import com.thms.service.TheatreService;
import com.thms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRestController {

    private final UserService userService;
    private final MovieService movieService;
    private final TheatreService theatreService;
    private final BookingService bookingService;

    public AdminRestController(UserService userService, MovieService movieService,
                              TheatreService theatreService, BookingService bookingService) {
        this.userService = userService;
        this.movieService = movieService;
        this.theatreService = theatreService;
        this.bookingService = bookingService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<Map<String, Object>>> dashboard() {
        Map<String, Object> dashboardData = new HashMap<>();
        
        // Total counts for dashboard cards
        dashboardData.put("totalUsers", userService.getAllUsers().size());
        dashboardData.put("totalMovies", movieService.getAllMovies().size());
        dashboardData.put("totalTheatres", theatreService.getAllTheatres().size());
        dashboardData.put("totalBookings", bookingService.getAllBookings().size());

        // Get recent bookings (last 5)
        List<BookingDTO> recentBookings = bookingService.getAllBookings().stream()
                .sorted(Comparator.comparing(BookingDTO::getBookingTime).reversed())
                .limit(5)
                .collect(Collectors.toList());
        dashboardData.put("recentBookings", recentBookings);

        // Get new users (last 5)
        List<UserDTO> newUsers = userService.getAllUsers().stream()
                .sorted(Comparator.comparing(UserDTO::getId).reversed())  // Assuming newer users have higher IDs
                .limit(5)
                .collect(Collectors.toList());
        dashboardData.put("newUsers", newUsers);

        // Get popular movies (movies with most bookings)
        List<MovieDTO> popularMovies = movieService.getAllMovies().stream()
                .limit(5)
                .collect(Collectors.toList());
        dashboardData.put("popularMovies", popularMovies);

        // Get upcoming screenings (next 24 hours)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        dashboardData.put("upcomingScreenings", bookingService.getBookingsByDateRange(now, tomorrow));

        // Get booking statistics by status
        long completedBookings = bookingService.getAllBookings().stream()
                .filter(b -> b.getPaymentStatus() == Booking.PaymentStatus.COMPLETED)
                .count();
        long pendingBookings = bookingService.getAllBookings().stream()
                .filter(b -> b.getPaymentStatus() == Booking.PaymentStatus.PENDING)
                .count();
        long cancelledBookings = bookingService.getAllBookings().stream()
                .filter(b -> b.getPaymentStatus() == Booking.PaymentStatus.CANCELLED)
                .count();

        Map<String, Long> bookingStats = new HashMap<>();
        bookingStats.put("completed", completedBookings);
        bookingStats.put("pending", pendingBookings);
        bookingStats.put("cancelled", cancelledBookings);
        dashboardData.put("bookingStats", bookingStats);

        return ResponseEntity.ok(ApiResponse.success(dashboardData));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserDetails(@PathVariable("id") Long id) {
        UserDTO user = userService.getUserById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("bookings", bookingService.getBookingsByUser(id));
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UserDTO userDTO) {
        
        UserDTO updatedUser = userService.updateUser(id, userDTO)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
                
        return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
    }
} 