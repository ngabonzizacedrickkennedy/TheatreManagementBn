package com.thms.controller;

import com.thms.dto.MovieDTO;
import com.thms.dto.ScreeningDTO;
import com.thms.service.MovieService;
import com.thms.service.ScreeningService;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final MovieService movieService;
    private final ScreeningService screeningService;

    public HomeController(MovieService movieService, ScreeningService screeningService) {
        this.movieService = movieService;
        this.screeningService = screeningService;
    }

    @GetMapping("/")
    public String home(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        // Get upcoming screenings (today and future)
        Set<String> roles = AuthorityUtils.authorityListToSet(userDetails.getAuthorities());
        if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_MANAGER")) {
            // Redirect admin to dashboard
            return "redirect:/admin/dashboard";
        }
        LocalDateTime now = LocalDateTime.now();
        List<ScreeningDTO> upcomingScreenings = screeningService.getUpcomingScreenings(now);

        // Group screenings by movie
        Map<Long, List<ScreeningDTO>> screeningsByMovie = upcomingScreenings.stream()
                .collect(Collectors.groupingBy(ScreeningDTO::getMovieId));

        // Get all current movies that have screenings
        List<MovieDTO> moviesWithScreenings = movieService.getMoviesByIds(screeningsByMovie.keySet());

        // Get upcoming movies (with future release dates)
        List<MovieDTO> upcomingMovies = movieService.getUpcomingMovies();

        model.addAttribute("nowPlaying", moviesWithScreenings);
        model.addAttribute("screeningsByMovie", screeningsByMovie);
        model.addAttribute("upcoming", upcomingMovies);

        return "home";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
    @PostMapping("/contact")
    public String processContactForm(
            @RequestParam("name") String name,
            @RequestParam("email") String email,
            @RequestParam("subject") String subject,
            @RequestParam("message") String message,
            RedirectAttributes redirectAttributes) {

        // In a real application, you would process the form data here
        // For example, send an email or save to database

        // For now, just show a success message
        redirectAttributes.addFlashAttribute("successMessage",
                "Thank you for your message! We'll get back to you shortly.");

        return "redirect:/contact";
    }
}