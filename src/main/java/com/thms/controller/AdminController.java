package com.thms.controller;

import com.thms.dto.BookingDTO;
import com.thms.dto.MovieDTO;
import com.thms.dto.TheatreDTO;
import com.thms.dto.UserDTO;
import com.thms.model.Booking;
import com.thms.service.BookingService;
import com.thms.service.MovieService;
import com.thms.service.TheatreService;
import com.thms.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final MovieService movieService;
    private final TheatreService theatreService;
    private final BookingService bookingService;

    public AdminController(UserService userService, MovieService movieService,
                           TheatreService theatreService, BookingService bookingService) {
        this.userService = userService;
        this.movieService = movieService;
        this.theatreService = theatreService;
        this.bookingService = bookingService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Total counts for dashboard cards
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalMovies", movieService.getAllMovies().size());
        model.addAttribute("totalTheatres", theatreService.getAllTheatres().size());
        model.addAttribute("totalBookings", bookingService.getAllBookings().size());

        // Get recent bookings (last 10)
        List<BookingDTO> recentBookings = bookingService.getAllBookings().stream()
                .sorted(Comparator.comparing(BookingDTO::getBookingTime).reversed())
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentBookings", recentBookings);

        // Get new users (last 10)
        List<UserDTO> newUsers = userService.getAllUsers().stream()
                .sorted(Comparator.comparing(UserDTO::getId).reversed())  // Assuming newer users have higher IDs
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("newUsers", newUsers);

        // Get popular movies (movies with most bookings)
        List<MovieDTO> popularMovies = movieService.getAllMovies().stream()
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("popularMovies", popularMovies);

        // Get upcoming screenings (next 24 hours)
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime tomorrow = now.plusDays(1);
        model.addAttribute("upcomingScreenings", bookingService.getBookingsByDateRange(now, tomorrow));

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

        model.addAttribute("completedBookings", completedBookings);
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("cancelledBookings", cancelledBookings);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<UserDTO> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String viewUser(@PathVariable("id") Long id, Model model) {
        UserDTO user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        model.addAttribute("user", user);
        model.addAttribute("bookings", bookingService.getBookingsByUser(id));
        return "admin/user-detail";
    }

    @GetMapping("/users/{id}/edit")
    public String editUserForm(@PathVariable("id") Long id, Model model) {
        UserDTO user = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        model.addAttribute("user", user);
        return "admin/user-edit";
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable("id") Long id,
                             @ModelAttribute("user") UserDTO userDTO,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.updateUser(id, userDTO);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users/" + id;
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }
}