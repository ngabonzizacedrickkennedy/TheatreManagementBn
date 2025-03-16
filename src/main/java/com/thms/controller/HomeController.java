package com.thms.controller;

import com.thms.dto.MovieDTO;
import com.thms.service.MovieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final MovieService movieService;

    public HomeController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<MovieDTO> nowPlaying = movieService.getCurrentlyPlayingMovies();
        List<MovieDTO> upcoming = movieService.getUpcomingMovies();
        
        model.addAttribute("nowPlaying", nowPlaying);
        model.addAttribute("upcoming", upcoming);
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