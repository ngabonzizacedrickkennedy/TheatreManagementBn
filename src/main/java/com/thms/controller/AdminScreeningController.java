package com.thms.controller;

import com.thms.dto.ScreeningDTO;
import com.thms.model.Screening;
import com.thms.service.BookingService;
import com.thms.service.MovieService;
import com.thms.service.ScreeningService;
import com.thms.service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/screenings")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminScreeningController {

    private final ScreeningService screeningService;
    private final MovieService movieService;
    private final TheatreService theatreService;
    private final BookingService bookingService;

    public AdminScreeningController(
            ScreeningService screeningService,
            MovieService movieService,
            TheatreService theatreService,
            BookingService bookingService) {
        this.screeningService = screeningService;
        this.movieService = movieService;
        this.theatreService = theatreService;
        this.bookingService = bookingService;
    }

    @GetMapping
    public String listScreenings(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long theatreId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
            Model model) {

        List<ScreeningDTO> screenings;

        // Apply filters if provided
        if (movieId != null && theatreId != null && date != null) {
            LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
            screenings = screeningService.getAvailableScreenings(movieId, theatreId, startOfDay);
        } else if (movieId != null && theatreId != null) {
            screenings = screeningService.getScreeningsByMovieAndTheatre(movieId, theatreId);
        } else if (movieId != null) {
            screenings = screeningService.getScreeningsByMovie(movieId);
        } else if (theatreId != null) {
            screenings = screeningService.getScreeningsByTheatre(theatreId);
        } else if (date != null) {
            LocalDateTime startOfDay = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endOfDay = LocalDateTime.of(date, LocalTime.MAX);
            screenings = screeningService.getScreeningsByDateRange(startOfDay, endOfDay);
        } else {
            screenings = screeningService.getAllScreenings();
        }

        model.addAttribute("screenings", screenings);
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("theatres", theatreService.getAllTheatres());

        // Keep filter values for form
        if (movieId != null) model.addAttribute("selectedMovieId", movieId);
        if (theatreId != null) model.addAttribute("selectedTheatreId", theatreId);
        if (date != null) model.addAttribute("selectedDate", date);

        return "admin/screenings";
    }

    @GetMapping("/create")
    public String createScreeningForm(
            @RequestParam(required = false) Long movieId,
            @RequestParam(required = false) Long theatreId,
            Model model) {

        ScreeningDTO screeningDTO = new ScreeningDTO();

        // Pre-select movie and theatre if provided
        if (movieId != null) screeningDTO.setMovieId(movieId);
        if (theatreId != null) screeningDTO.setTheatreId(theatreId);

        model.addAttribute("screening", screeningDTO);
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("theatres", theatreService.getAllTheatres());
        model.addAttribute("formats", Screening.ScreeningFormat.values());

        return "admin/screenings/create";
    }

    @PostMapping("/create")
    public String createScreening(
            @ModelAttribute("screening") ScreeningDTO screeningDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("theatres", theatreService.getAllTheatres());
            model.addAttribute("formats", Screening.ScreeningFormat.values());
            System.out.println("in screenings !!"+result.getAllErrors());
            return "admin/screenings/create";
        }

        try {
            ScreeningDTO created = screeningService.createScreening(screeningDTO);
            System.out.println("Great !!! "+created);
            redirectAttributes.addFlashAttribute("successMessage", "Screening created successfully!");
            return "redirect:/admin/screenings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating screening: " + e.getMessage());
            return "redirect:/admin/screenings/create";
        }
    }

    @GetMapping("/{id}")
    public String viewScreening(@PathVariable("id") Long id, Model model) {
        ScreeningDTO screening = screeningService.getScreeningById(id)
                .orElseThrow(() -> new RuntimeException("Screening not found with id: " + id));

        model.addAttribute("screening", screening);
        model.addAttribute("bookedSeats", bookingService.getBookedSeatsByScreeningId(id));
        return "admin/screenings/view";
    }

    @GetMapping("/{id}/edit")
    public String editScreeningForm(@PathVariable("id") Long id, Model model) {
        ScreeningDTO screening = screeningService.getScreeningById(id)
                .orElseThrow(() -> new RuntimeException("Screening not found with id: " + id));

        model.addAttribute("screening", screening);
        model.addAttribute("movies", movieService.getAllMovies());
        model.addAttribute("theatres", theatreService.getAllTheatres());
        model.addAttribute("formats", Screening.ScreeningFormat.values());
        return "admin/screenings/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateScreening(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("screening") ScreeningDTO screeningDTO,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("movies", movieService.getAllMovies());
            model.addAttribute("theatres", theatreService.getAllTheatres());
            model.addAttribute("formats", Screening.ScreeningFormat.values());
            return "admin/screenings/edit";
        }

        try {
            screeningService.updateScreening(id, screeningDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Screening updated successfully!");
            return "redirect:/admin/screenings";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating screening: " + e.getMessage());
            return "redirect:/admin/screenings/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteScreening(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            screeningService.deleteScreening(id);
            redirectAttributes.addFlashAttribute("successMessage", "Screening deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting screening: " + e.getMessage());
        }
        return "redirect:/admin/screenings";
    }

    @GetMapping("/{id}/bookings")
    public String viewScreeningBookings(@PathVariable("id") Long id, Model model) {
        ScreeningDTO screening = screeningService.getScreeningById(id)
                .orElseThrow(() -> new RuntimeException("Screening not found with id: " + id));

        model.addAttribute("screening", screening);
        model.addAttribute("bookings", bookingService.getBookingsByScreeningId(id));
        return "admin/screenings/bookings";
    }
}