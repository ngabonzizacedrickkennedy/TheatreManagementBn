package com.thms.controller;

import com.thms.dto.MovieDTO;
import com.thms.model.Movie;
import com.thms.service.MovieService;
import com.thms.service.ScreeningService;
import com.thms.service.TheatreService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

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
    public String getAllMovies(Model model) {
        List<MovieDTO> movies = movieService.getAllMovies();
        model.addAttribute("movies", movies);
        return "admin/movies";
    }

    @GetMapping("/{id}")
    public String getMovieDetails(@PathVariable("id") Long id, Model model) {
        MovieDTO movie = movieService.getMovieById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        
        model.addAttribute("movie", movie);
        model.addAttribute("theatres", theatreService.getTheatresByMovie(id));
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
        return "movies/list";
    }

    @GetMapping("/genre/{genre}")
    public String getMoviesByGenre(@PathVariable("genre") String genre, Model model) {
        try {
            Movie.Genre movieGenre = Movie.Genre.valueOf(genre.toUpperCase());
            List<MovieDTO> movies = movieService.getMoviesByGenre(movieGenre);
            model.addAttribute("movies", movies);
            model.addAttribute("selectedGenre", genre);
            return "movies/list";
        } catch (IllegalArgumentException e) {
            return "redirect:/movies";
        }
    }

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