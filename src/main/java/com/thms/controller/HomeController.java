package com.thms.controller;

import com.thms.dto.MovieDTO;
import com.thms.dto.ScreeningDTO;
import com.thms.service.MovieService;
import com.thms.service.ScreeningService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    public String home(Model model) {
        // Get upcoming screenings (today and future)
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
}