package com.thms.controller;

import com.thms.dto.BookingDTO;
import com.thms.model.Booking;
import com.thms.service.BookingService;
import com.thms.service.MovieService;
import com.thms.service.TheatreService;
import com.thms.service.UserService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/bookings")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminBookingController {

    private final BookingService bookingService;
    private final UserService userService;
    private final MovieService movieService;
    private final TheatreService theatreService;

    public AdminBookingController(BookingService bookingService, 
                                 UserService userService,
                                 MovieService movieService,
                                 TheatreService theatreService) {
        this.bookingService = bookingService;
        this.userService = userService;
        this.movieService = movieService;
        this.theatreService = theatreService;
    }

    @GetMapping
    public String getAllBookings(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long theatreId,
            @RequestParam(required = false) String bookingNumber,
            @RequestParam(required = false) Booking.PaymentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            Model model) {

        List<BookingDTO> bookings;

        // Handle different filtering options
        if (userId != null) {
            bookings = bookingService.getBookingsByUser(userId);
            model.addAttribute("selectedUserId", userId);
        } else if (movieId != null) {
            bookings = bookingService.getBookingsByMovie(movieId);
            model.addAttribute("selectedMovieId", movieId);
        } else if (theatreId != null) {
            bookings = bookingService.getBookingsByTheatre(theatreId);
            model.addAttribute("selectedTheatreId", theatreId);
        } else if (bookingNumber != null && !bookingNumber.isEmpty()) {
            bookingService.getBookingByNumber(bookingNumber)
                    .ifPresent(booking -> model.addAttribute("booking", booking));
            bookings = bookingService.getAllBookings(); // Load all for display
            model.addAttribute("bookingNumber", bookingNumber);
        } else if (status != null) {
            bookings = bookingService.getBookingsByStatus(status);
            model.addAttribute("selectedStatus", status);
        } else if (fromDate != null && toDate != null) {
            bookings = bookingService.getBookingsByDateRange(fromDate, toDate);
            model.addAttribute("fromDate", fromDate);
            model.addAttribute("toDate", toDate);
        } else {
            // Default - show all bookings
            bookings = bookingService.getAllBookings();
        }

        model.addAttribute("bookings", bookings);
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("theatres", theatreService.getAllTheatres());
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("statuses", Booking.PaymentStatus.values());

        return "admin/bookings/list";
    }

    @GetMapping("/{id}")
    public String viewBooking(@PathVariable("id") Long id, Model model) {
        BookingDTO booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

        model.addAttribute("booking", booking);
        return "admin/bookings/view";
    }

    @PostMapping("/{id}/update-status")
    public String updateBookingStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") Booking.PaymentStatus status,
            RedirectAttributes redirectAttributes) {

        try {
            bookingService.updateBookingStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Booking status updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating booking: " + e.getMessage());
        }

        return "redirect:/admin/bookings/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteBooking(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {

        try {
            bookingService.deleteBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting booking: " + e.getMessage());
        }

        return "redirect:/admin/bookings";
    }

    @GetMapping("/export")
    public String exportBookings(Model model) {
        // In a real application, you would handle exporting data to CSV/Excel here
        // For now, we'll just show a message
        model.addAttribute("exportMessage", "Export functionality will be implemented soon.");
        model.addAttribute("bookings", bookingService.getAllBookings());
        return "admin/bookings/list";
    }
}