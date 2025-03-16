package com.thms.controller;

import com.thms.dto.MovieDTO;
import com.thms.model.Movie;
import com.thms.service.MovieService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;

@Controller
@RequestMapping("/admin/movies")
@PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
public class AdminMovieController {

    private final MovieService movieService;

    public AdminMovieController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public String listMovies(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false, defaultValue = "id") String sortBy,
            Model model) {
        
        // Handle search and filtering
        if (search != null && !search.isEmpty()) {
            model.addAttribute("movies", movieService.searchMoviesByTitle(search));
            model.addAttribute("search", search);
        } else if (genre != null && !genre.isEmpty()) {
            try {
                Movie.Genre genreEnum = Movie.Genre.valueOf(genre);
                model.addAttribute("movies", movieService.getMoviesByGenre(genreEnum));
                model.addAttribute("genre", genre);
            } catch (IllegalArgumentException e) {
                model.addAttribute("movies", movieService.getAllMovies());
            }
        } else {
            model.addAttribute("movies", movieService.getAllMovies());
        }
        
        return "admin/movies";
    }
    
    @GetMapping("/create")
    public String createMovieForm(Model model) {
        model.addAttribute("movie", new MovieDTO());
        model.addAttribute("genres", Movie.Genre.values());
        model.addAttribute("ratings", Movie.Rating.values());
        return "admin/movies/create";
    }
    
    @PostMapping("/create")
    public String createMovie(@Valid @ModelAttribute("movie") MovieDTO movieDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("genres", Movie.Genre.values());
            model.addAttribute("ratings", Movie.Rating.values());
            return "admin/movies/create";
        }
        
        try {
            MovieDTO createdMovie = movieService.createMovie(movieDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Movie created successfully!");
            return "redirect:/admin/movies";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating movie: " + e.getMessage());
            return "redirect:/admin/movies/create";
        }
    }
    
    @GetMapping("/{id}")
    public String viewMovie(@PathVariable("id") Long id, Model model) {
        MovieDTO movie = movieService.getMovieById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        
        model.addAttribute("movie", movie);
        return "admin/movies/view";
    }
    
    @GetMapping("/{id}/edit")
    public String editMovieForm(@PathVariable("id") Long id, Model model) {
        MovieDTO movie = movieService.getMovieById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        
        model.addAttribute("movie", movie);
        model.addAttribute("genres", Movie.Genre.values());
        model.addAttribute("ratings", Movie.Rating.values());
        return "admin/movies/edit";
    }
    
    @PostMapping("/{id}/edit")
    public String updateMovie(@PathVariable("id") Long id,
                             @Valid @ModelAttribute("movie") MovieDTO movieDTO,
                             BindingResult result,
                             RedirectAttributes redirectAttributes,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("genres", Movie.Genre.values());
            model.addAttribute("ratings", Movie.Rating.values());
            return "admin/movies/edit";
        }
        
        try {
            movieService.updateMovie(id, movieDTO);
            redirectAttributes.addFlashAttribute("successMessage", "Movie updated successfully!");
            return "redirect:/admin/movies";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating movie: " + e.getMessage());
            return "redirect:/admin/movies/" + id + "/edit";
        }
    }
    
    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteMovie(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            movieService.deleteMovie(id);
            redirectAttributes.addFlashAttribute("successMessage", "Movie deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting movie: " + e.getMessage());
        }
        return "redirect:/admin/movies";
    }
    
    @GetMapping("/{id}/screenings")
    public String viewMovieScreenings(@PathVariable("id") Long id, Model model) {
        MovieDTO movie = movieService.getMovieById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + id));
        
        model.addAttribute("movie", movie);
        // You'll need to implement this method in your service
        // model.addAttribute("screenings", screeningService.getScreeningsByMovie(id));
        
        return "admin/movies/";
    }
}