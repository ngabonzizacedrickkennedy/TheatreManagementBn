package com.thms.controller;

import com.thms.model.Seat;
import com.thms.model.Theatre;
import com.thms.service.SeatService;
import com.thms.service.TheatreService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/seats")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSeatController {

    private final SeatService seatService;
    private final TheatreService theatreService;

    public AdminSeatController(SeatService seatService, TheatreService theatreService) {
        this.seatService = seatService;
        this.theatreService = theatreService;
    }

    @GetMapping("/theatre/{theatreId}")
    public String listTheatreScreens(@PathVariable("theatreId") Long theatreId, Model model) {
        Theatre theatre = theatreService.getTheatreEntityById(theatreId)
                .orElseThrow(() -> new RuntimeException("Theatre not found with id: " + theatreId));
        
        model.addAttribute("theatre", theatre);
        model.addAttribute("screens", theatre.getTotalScreens());
        return "admin/seats/screens";
    }

    @GetMapping("/theatre/{theatreId}/screen/{screenNumber}")
    public String manageSeatsByScreen(@PathVariable("theatreId") Long theatreId, 
                                     @PathVariable("screenNumber") Integer screenNumber,
                                     Model model) {
        Theatre theatre = theatreService.getTheatreEntityById(theatreId)
                .orElseThrow(() -> new RuntimeException("Theatre not found with id: " + theatreId));
        
        // Get seats grouped by row
        Map<String, List<Seat>> seatsByRow = seatService.getSeatMapByTheatreAndScreen(theatreId, screenNumber);
        
        model.addAttribute("theatre", theatre);
        model.addAttribute("screenNumber", screenNumber);
        model.addAttribute("seatsByRow", seatsByRow);
        model.addAttribute("seatTypes", Seat.SeatType.values());
        return "admin/seats/manage";
    }

    @PostMapping("/theatre/{theatreId}/screen/{screenNumber}/initialize")
    public String initializeSeats(@PathVariable("theatreId") Long theatreId,
                                 @PathVariable("screenNumber") Integer screenNumber,
                                 @RequestParam("rows") Integer rows,
                                 @RequestParam("seatsPerRow") Integer seatsPerRow,
                                 RedirectAttributes redirectAttributes) {
        try {
            seatService.initializeSeatsForTheatre(theatreId, screenNumber, rows, seatsPerRow);
            redirectAttributes.addFlashAttribute("successMessage", 
                    String.format("Successfully initialized %d seats for screen %d", rows * seatsPerRow, screenNumber));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error initializing seats: " + e.getMessage());
        }
        return "redirect:/admin/seats/theatre/" + theatreId + "/screen/" + screenNumber;
    }

    @PostMapping("/update/{seatId}")
    public String updateSeat(@PathVariable("seatId") Long seatId,
                            @RequestParam("seatType") Seat.SeatType seatType,
                            @RequestParam("priceMultiplier") Double priceMultiplier,
                            RedirectAttributes redirectAttributes) {
        try {
            Seat seat = seatService.getSeatById(seatId)
                    .orElseThrow(() -> new RuntimeException("Seat not found with id: " + seatId));
            
            seatService.updateSeatType(seatId, seatType, priceMultiplier);
            
            redirectAttributes.addFlashAttribute("successMessage", "Seat updated successfully");
            return "redirect:/admin/seats/theatre/" + seat.getTheatre().getId() + "/screen/" + seat.getScreenNumber();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating seat: " + e.getMessage());
            return "redirect:/admin/theatres";
        }
    }

    @PostMapping("/updateRow")
    public String updateSeatRow(@RequestParam("theatreId") Long theatreId,
                               @RequestParam("screenNumber") Integer screenNumber,
                               @RequestParam("rowName") String rowName,
                               @RequestParam("seatType") Seat.SeatType seatType,
                               @RequestParam("priceMultiplier") Double priceMultiplier,
                               RedirectAttributes redirectAttributes) {
        try {
            seatService.updateSeatRowType(theatreId, screenNumber, rowName, seatType, priceMultiplier);
            redirectAttributes.addFlashAttribute("successMessage", "Row " + rowName + " updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating row: " + e.getMessage());
        }
        return "redirect:/admin/seats/theatre/" + theatreId + "/screen/" + screenNumber;
    }

    @PostMapping("/bulkUpdate")
    public String bulkUpdateSeats(@RequestParam("theatreId") Long theatreId,
                                 @RequestParam("screenNumber") Integer screenNumber,
                                 @RequestParam("selection") String selection,
                                 @RequestParam("seatType") Seat.SeatType seatType,
                                 @RequestParam("priceMultiplier") Double priceMultiplier,
                                 RedirectAttributes redirectAttributes) {
        try {
            String[] seatIds = selection.split(",");
            int updatedCount = seatService.bulkUpdateSeats(Arrays.asList(seatIds), theatreId, screenNumber, seatType, priceMultiplier);
            
            redirectAttributes.addFlashAttribute("successMessage", 
                    String.format("Successfully updated %d seats", updatedCount));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating seats: " + e.getMessage());
        }
        return "redirect:/admin/seats/theatre/" + theatreId + "/screen/" + screenNumber;
    }

    @PostMapping("/theatre/{theatreId}/screen/{screenNumber}/delete")
    public String deleteScreenSeats(@PathVariable("theatreId") Long theatreId,
                                   @PathVariable("screenNumber") Integer screenNumber,
                                   RedirectAttributes redirectAttributes) {
        try {
            int deletedCount = seatService.deleteScreenSeats(theatreId, screenNumber);
            redirectAttributes.addFlashAttribute("successMessage", 
                    String.format("Successfully deleted %d seats from screen %d", deletedCount, screenNumber));
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting seats: " + e.getMessage());
        }
        return "redirect:/admin/seats/theatre/" + theatreId;
    }
}