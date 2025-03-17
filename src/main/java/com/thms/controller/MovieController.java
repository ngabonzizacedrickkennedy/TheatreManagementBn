package com.thms.controller;

import com.thms.dto.MovieDTO;
import com.thms.dto.ScreeningDTO;
import com.thms.model.Movie;
import com.thms.service.MovieService;
import com.thms.service.ScreeningService;
import com.thms.service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;
    private final TheatreService theatreService;
    private final ScreeningService screeningService;

    public MovieController(MovieService movieService, TheatreService theatreService, ScreeningService screeningService) {
        this.movieService = movieService;
        this.theatreService = theatreService;
        this.screeningService = screeningService;
    }

    @GetMapping
    public String getAllMovies(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "genre", required = false) String genre,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Model model) {

        // Get current user role
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        List<MovieDTO> movies;

        // Get movies based on filters
        if (query != null && !query.isEmpty()) {
            movies = movieService.searchMoviesByTitle(query);
            model.addAttribute("searchQuery", query);
        } else if (genre != null && !genre.isEmpty()) {
            try {
                Movie.Genre movieGenre = Movie.Genre.valueOf(genre.toUpperCase());
                movies = movieService.getMoviesByGenre(movieGenre);
                model.addAttribute("selectedGenre", genre);
            } catch (IllegalArgumentException e) {
                movies = movieService.getAllMovies();
            }
        } else {
            movies = movieService.getAllMovies();
        }

        model.addAttribute("movies", movies);

        // For user view, add screenings data
        if (!isAdmin) {
            // Get screenings for today or specified date
            LocalDateTime startDateTime;
            LocalDateTime endDateTime;

            if (date != null) {
                startDateTime = LocalDateTime.of(date, LocalTime.MIDNIGHT);
                endDateTime = LocalDateTime.of(date, LocalTime.MAX);
            } else {
                // Default to today
                LocalDate today = LocalDate.now();
                startDateTime = LocalDateTime.of(today, LocalTime.MIDNIGHT);
                endDateTime = LocalDateTime.of(today, LocalTime.MAX);
            }

            List<ScreeningDTO> screenings = screeningService.getScreeningsByDateRange(startDateTime, endDateTime);

            // Group screenings by movie ID
            Map<Long, List<ScreeningDTO>> screeningsByMovie = screenings.stream()
                    .collect(Collectors.groupingBy(ScreeningDTO::getMovieId));

            model.addAttribute("screeningsByMovie", screeningsByMovie);
        }

        // Return the appropriate view based on user role
        if (isAdmin) {
            return "admin/movies";
        } else {
            return "movies/list";
        }

    }




    @GetMapping("/{id}")
    public String getMovieDetails(@PathVariable("id") Long id, Model model) {
        MovieDTO movie = movieService.getMovieById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

        model.addAttribute("movie", movie);
        model.addAttribute("theatres", theatreService.getTheatresByMovie(id));

        // Get screenings for this movie (next 7 days)
        LocalDateTime startDateTime = LocalDateTime.now();
        LocalDateTime endDateTime = startDateTime.plusDays(7);

        List<ScreeningDTO> screenings = screeningService.getScreeningsByMovie(id)
                .stream()
                .filter(s -> s.getStartTime().isAfter(startDateTime) && s.getStartTime().isBefore(endDateTime))
                .collect(Collectors.toList());

        // Group screenings by date
        Map<LocalDate, List<ScreeningDTO>> screeningsByDate = screenings.stream()
                .collect(Collectors.groupingBy(s -> s.getStartTime().toLocalDate()));

        model.addAttribute("screeningsByDate", screeningsByDate);

        return "movies/detail";
    }

    @GetMapping("/search")
    public String searchMovies(@RequestParam(value = "query", required = false) String query, Model model) {
        List<MovieDTO> movies;

        if (query != null && !query.isEmpty()) {
            movies = movieService.searchMoviesByTitle(query);
            model.addAttribute("searchQuery", query);
        } else {
            movies = movieService.getAllMovies();
        }

        model.addAttribute("movies", movies);

        // Get current user role
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        // Return the appropriate view based on user role
        if (isAdmin) {
            return "admin/movies";
        } else {
            return "movies/list";
        }
    }

    @GetMapping("/genre/{genre}")
    public String getMoviesByGenre(@PathVariable("genre") String genre, Model model) {
        try {
            Movie.Genre movieGenre = Movie.Genre.valueOf(genre.toUpperCase());
            List<MovieDTO> movies = movieService.getMoviesByGenre(movieGenre);
            model.addAttribute("movies", movies);
            model.addAttribute("selectedGenre", genre);

            // Get current user role
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

            // Return the appropriate view based on user role
            if (isAdmin) {
                return "admin/movies";
            } else {
                return "movies/list";
            }
        } catch (IllegalArgumentException e) {
            return "redirect:/movies";
        }
    }

    // Admin-only methods below

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/create")
    public String createMovieForm(Model model) {
        model.addAttribute("movie", new MovieDTO());
        model.addAttribute("genres", Arrays.asList(Movie.Genre.values()));
        model.addAttribute("ratings", Arrays.asList(Movie.Rating.values()));
        return "admin/movies/create";
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping("/create")
    public String createMovie(@Valid @ModelAttribute("movie") MovieDTO movieDTO,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("genres", Arrays.asList(Movie.Genre.values()));
            model.addAttribute("ratings", Arrays.asList(Movie.Rating.values()));
            return "admin/movies/create";
        }

        try {
            MovieDTO createdMovie = movieService.createMovie(movieDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Movie created successfully!");
            return "redirect:/movies/" + createdMovie.getId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/movies/create";
        }
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @GetMapping("/{id}/edit")
    public String editMovieForm(@PathVariable("id") Long id, Model model) {
        MovieDTO movie = movieService.getMovieById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));

        model.addAttribute("movie", movie);
        model.addAttribute("genres", Arrays.asList(Movie.Genre.values()));
        model.addAttribute("ratings", Arrays.asList(Movie.Rating.values()));
        return "admin/movies/edit";
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('MANAGER')")
    @PostMapping("/{id}/edit")
    public String updateMovie(@PathVariable("id") Long id,
                              @Valid @ModelAttribute("movie") MovieDTO movieDTO,
                              BindingResult result,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (result.hasErrors()) {
            model.addAttribute("genres", Arrays.asList(Movie.Genre.values()));
            model.addAttribute("ratings", Arrays.asList(Movie.Rating.values()));
            return "admin/movies/edit";
        }

        try {
            movieService.updateMovie(id, movieDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Movie updated successfully!");
            return "redirect:/movies/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/movies/" + id + "/edit";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteMovie(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            movieService.deleteMovie(id);
            redirectAttributes.addFlashAttribute("successMessage", "Movie deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/movies";
    }
}