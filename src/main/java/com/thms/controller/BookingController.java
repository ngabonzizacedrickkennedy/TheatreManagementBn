package com.thms.controller;

import com.thms.dto.BookingDTO;
import com.thms.model.Booking;
import com.thms.model.Screening;
import com.thms.model.User;
import com.thms.service.BookingService;
import com.thms.service.ScreeningService;
import com.thms.service.SeatService;
import com.thms.dto.BookingDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;
    private final ScreeningService screeningService;
    private final SeatService seatService;

    public BookingController(BookingService bookingService, ScreeningService screeningService, SeatService seatService) {
        this.bookingService = bookingService;
        this.screeningService = screeningService;
        this.seatService = seatService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public String myBookings(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        List<BookingDTO> bookings = bookingService.getBookingsByUsername(username);
        model.addAttribute("bookings", bookings);
        return "booking/bookings";
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}")
    public String viewBooking(@PathVariable("id") Long id, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        
        BookingDTO booking = bookingService.getBookingById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        
        // Security check: ensure the user can only view their own bookings or admin/manager can view any
        if (!booking.getUsername().equals(username) && 
            !(auth.getAuthorities().stream().anyMatch(r -> 
                r.getAuthority().equals("ROLE_ADMIN") || r.getAuthority().equals("ROLE_MANAGER")))) {
            return "redirect:/error/403";
        }
        
        model.addAttribute("booking", booking);
        return "booking/view";
    }

    @GetMapping("/screening/{screeningId}")
    public String selectSeats(@PathVariable("screeningId") Long screeningId, Model model) {
        try {
            Screening screening = screeningService.getScreeningEntityById(screeningId)
                    .orElseThrow(() -> new RuntimeException("Screening not found with id: " + screeningId));

            // Get theatre seats
            var seats = seatService.getSeatsByTheatreAndScreen(screening.getTheatre().getId(), screening.getScreenNumber());

            // Get already booked seats
            Set<String> bookedSeats = bookingService.getBookedSeatsByScreeningId(screeningId);

            // Debug logging
//            System.out.println("Screening: " + screening);
//            System.out.println("Movie: " + screening.getMovie());
//            System.out.println("Theatre: " + screening.getTheatre());
//            System.out.println("Seats size: " + seats.size());
//            System.out.println("Booked seats: " + bookedSeats);

            model.addAttribute("screening", screening);
            model.addAttribute("movie", screening.getMovie());
            model.addAttribute("theatre", screening.getTheatre());
            model.addAttribute("seats", seats);
            model.addAttribute("bookedSeats", bookedSeats);

            return "booking/seat-selection";
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/screening/{screeningId}")
    public String processSelectedSeats(@PathVariable("screeningId") Long screeningId,
                                       @RequestParam("selectedSeats") List<String> selectedSeats,
                                       RedirectAttributes redirectAttributes) {
        if (selectedSeats.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Please select at least one seat.");
            return "redirect:/bookings/screening/" + screeningId;
        }
        
        // Store selected seats in session or pass to checkout
        redirectAttributes.addFlashAttribute("selectedSeats", selectedSeats);
        return "redirect:/bookings/checkout/" + screeningId;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/checkout/{screeningId}")
    public String checkout(@PathVariable("screeningId") Long screeningId,
                          @ModelAttribute("selectedSeats") List<String> selectedSeats,
                          Model model) {
        if (selectedSeats == null || selectedSeats.isEmpty()) {
            return "redirect:/bookings/screening/" + screeningId;
        }
        
        Screening screening = screeningService.getScreeningEntityById(screeningId)
            .orElseThrow(() -> new RuntimeException("Screening not found with id: " + screeningId));
        
        // Calculate total price
        double totalPrice = bookingService.calculateTotalPrice(screeningId, selectedSeats);
        
        model.addAttribute("screening", screening);
        model.addAttribute("movie", screening.getMovie());
        model.addAttribute("theatre", screening.getTheatre());
        model.addAttribute("selectedSeats", selectedSeats);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("paymentMethods", Arrays.asList("Credit Card", "Debit Card", "PayPal"));
        
        return "booking/checkout";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/confirm")
    public String confirmBooking(@RequestParam("screeningId") Long screeningId,
                                @RequestParam("selectedSeats") List<String> selectedSeats,
                                @RequestParam("paymentMethod") String paymentMethod,
                                RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            BookingDTO booking = bookingService.createBooking(screeningId, username, selectedSeats, paymentMethod);
            redirectAttributes.addFlashAttribute("successMessage", "Booking confirmed successfully!");
            return "redirect:/bookings/" + booking.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/bookings/screening/" + screeningId;
        }
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/cancel")
    public String cancelBooking(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            
            BookingDTO booking = bookingService.getBookingById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
            
            // Security check: ensure the user can only cancel their own bookings or admin/manager can cancel any
            if (!booking.getUsername().equals(username) && 
                !(auth.getAuthorities().stream().anyMatch(r -> 
                    r.getAuthority().equals("ROLE_ADMIN") || r.getAuthority().equals("ROLE_MANAGER")))) {
                return "redirect:/error/403";
            }
            
            bookingService.cancelBooking(id);
            redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/bookings";
    }
}