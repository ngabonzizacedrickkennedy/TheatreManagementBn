package com.thms.service;

import com.thms.dto.MovieDTO;
import com.thms.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MovieService {

    // Existing methods
    List<MovieDTO> getAllMovies();
    Optional<MovieDTO> getMovieById(Long id);
    MovieDTO createMovie(MovieDTO movieDTO);
    Optional<MovieDTO> updateMovie(Long id, MovieDTO movieDTO);
    void deleteMovie(Long id);
    List<MovieDTO> searchMoviesByTitle(String title);
    List<MovieDTO> getMoviesByGenre(Movie.Genre genre);
    List<MovieDTO> getUpcomingMovies();
    List<MovieDTO> getMoviesByIds(Set<Long> ids);

    // New pagination methods
    Page<MovieDTO> getAllMovies(Pageable pageable);
    Page<MovieDTO> searchMoviesByTitle(String title, Pageable pageable);
    Page<MovieDTO> getMoviesByGenre(Movie.Genre genre, Pageable pageable);
    Page<MovieDTO> getUpcomingMovies(Pageable pageable);
}