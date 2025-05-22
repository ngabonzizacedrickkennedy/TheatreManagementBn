package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.dto.BookingDTO;
import com.thms.dto.MovieDTO;
import com.thms.service.BookingService;
import com.thms.service.MovieService;
import com.thms.service.TheatreService;
import com.thms.service.UserService;
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

        // Get new users (last 5) - Remove the detailed user methods, just get count
        dashboardData.put("newUsersCount", userService.getAllUsers().size());

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
                .filter(b -> b.getPaymentStatus() == com.thms.model.Booking.PaymentStatus.COMPLETED)
                .count();
        long pendingBookings = bookingService.getAllBookings().stream()
                .filter(b -> b.getPaymentStatus() == com.thms.model.Booking.PaymentStatus.PENDING)
                .count();
        long cancelledBookings = bookingService.getAllBookings().stream()
                .filter(b -> b.getPaymentStatus() == com.thms.model.Booking.PaymentStatus.CANCELLED)
                .count();

        Map<String, Long> bookingStats = new HashMap<>();
        bookingStats.put("completed", completedBookings);
        bookingStats.put("pending", pendingBookings);
        bookingStats.put("cancelled", cancelledBookings);
        dashboardData.put("bookingStats", bookingStats);

        return ResponseEntity.ok(ApiResponse.success(dashboardData));
    }

    // REMOVED: All user-related methods should be handled by AdminUsersRestController
    // The following methods have been removed to avoid ambiguous mapping:
    // - getUsers()
    // - getUserDetails()
    // - updateUser()
    // - deleteUser()

    // These methods are now exclusively handled by AdminUsersRestController
}