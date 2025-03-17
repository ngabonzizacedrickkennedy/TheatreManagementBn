package com.thms.controller;

import com.thms.dto.TheatreDTO;
import com.thms.service.TheatreService;
import com.thms.service.ScreeningService;
import com.thms.service.SeatService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/theatres")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminTheatreController {

    private final TheatreService theatreService;
    private final ScreeningService screeningService;
    private final SeatService seatService;

    public AdminTheatreController(
            TheatreService theatreService, 
            ScreeningService screeningService,
            SeatService seatService) {
        this.theatreService = theatreService;
        this.screeningService = screeningService;
        this.seatService = seatService;
    }

    @GetMapping
    public String listTheatres(
            @RequestParam(required = false) String search,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            Model model) {
        
        // Handle search functionality
        if (search != null && !search.isEmpty()) {
            model.addAttribute("theatres", theatreService.searchTheatresByName(search));
            model.addAttribute("search", search);
        } else {
            model.addAttribute("theatres", theatreService.getAllTheatres());
        }
        
        return "admin/theatres";
    }

    @GetMapping("/create")
    public String createTheatreForm(Model model) {
        model.addAttribute("theatre", new TheatreDTO());
        return "admin/theatres/create";
    }

    @PostMapping("/create")
    public String createTheatre(@Valid @ModelAttribute("theatre") TheatreDTO theatreDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/theatres/create";
        }
        
        try {
            TheatreDTO createdTheatre = theatreService.createTheatre(theatreDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Theatre created successfully!");
            return "redirect:/admin/theatres";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating theatre: " + e.getMessage());
            return "redirect:/admin/theatres/create";
        }
    }

    @GetMapping("/{id}")
    public String viewTheatre(@PathVariable("id") Long id, Model model) {
        TheatreDTO theatre = theatreService.getTheatreById(id)
                .orElseThrow(() -> new RuntimeException("Theatre not found with id: " + id));
        
        model.addAttribute("theatre", theatre);
        return "admin/theatres/view";
    }

    @GetMapping("/{id}/edit")
    public String editTheatreForm(@PathVariable("id") Long id, Model model) {
        TheatreDTO theatre = theatreService.getTheatreById(id)
                .orElseThrow(() -> new RuntimeException("Theatre not found with id: " + id));
        
        model.addAttribute("theatre", theatre);
        return "admin/theatres/edit";
    }

    @PostMapping("/{id}/edit")
    public String updateTheatre(@PathVariable("id") Long id,
                               @Valid @ModelAttribute("theatre") TheatreDTO theatreDTO,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/theatres/edit";
        }
        
        try {
            theatreService.updateTheatre(id, theatreDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Theatre updated successfully!");
            return "redirect:/admin/theatres";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating theatre: " + e.getMessage());
            return "redirect:/admin/theatres/" + id + "/edit";
        }
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteTheatre(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            theatreService.deleteTheatre(id);
            redirectAttributes.addFlashAttribute("successMessage", "Theatre deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting theatre: " + e.getMessage());
        }
        return "redirect:/admin/theatres";
    }
    
    @GetMapping("/{id}/seats")
    public String manageSeats(@PathVariable("id") Long id, Model model) {
        TheatreDTO theatre = theatreService.getTheatreById(id)
                .orElseThrow(() -> new RuntimeException("Theatre not found with id: " + id));
        
        model.addAttribute("theatre", theatre);
        // For each screen, get the seats
        for (int i = 1; i <= theatre.getTotalScreens(); i++) {
            model.addAttribute("seatsScreen" + i, seatService.getSeatsByTheatreAndScreen(id, i));
        }
        
        return "admin/theatres/seats";
    }
    
    @PostMapping("/{id}/seats/initialize")
    public String initializeSeats(
            @PathVariable("id") Long id,
            @RequestParam("screenNumber") Integer screenNumber,
            @RequestParam("rows") Integer rows,
            @RequestParam("seatsPerRow") Integer seatsPerRow,
            RedirectAttributes redirectAttributes) {
        try {
            seatService.initializeSeatsForTheatre(id, screenNumber, rows, seatsPerRow);
            redirectAttributes.addFlashAttribute("successMessage", 
                    "Seats initialized for Screen " + screenNumber + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Error initializing seats: " + e.getMessage());
        }
        return "redirect:/admin/theatres/" + id + "/seats";
    }
    
    @GetMapping("/{id}/screenings")
    public String viewTheatreScreenings(@PathVariable("id") Long id, Model model) {
        TheatreDTO theatre = theatreService.getTheatreById(id)
                .orElseThrow(() -> new RuntimeException("Theatre not found with id: " + id));
        
        model.addAttribute("theatre", theatre);
        model.addAttribute("screenings", screeningService.getScreeningsByTheatre(id));
        
        return "admin/theatres/screenings";
    }
}