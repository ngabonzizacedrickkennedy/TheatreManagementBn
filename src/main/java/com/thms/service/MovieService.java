package com.thms.service;

import com.thms.dto.MovieDTO;
import com.thms.model.Movie;
import com.thms.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class MovieService {

    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional
    public MovieDTO createMovie(MovieDTO movieDTO) {
        Movie movie = convertToEntity(movieDTO);
        Movie savedMovie = movieRepository.save(movie);
        return convertToDTO(savedMovie);
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<MovieDTO> getMovieById(Long id) {
        return movieRepository.findById(id).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> searchMoviesByTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getMoviesByGenre(Movie.Genre genre) {
        return movieRepository.findByGenre(genre).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getUpcomingMovies() {
        return movieRepository.findUpcomingMovies().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getCurrentlyPlayingMovies() {
        return movieRepository.findCurrentlyPlayingMovies().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MovieDTO> getMoviesByTheatre(Long theatreId) {
        return movieRepository.findMoviesByTheatreId(theatreId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<MovieDTO> updateMovie(Long id, MovieDTO movieDTO) {
        return movieRepository.findById(id).map(movie -> {
            movie.setTitle(movieDTO.getTitle());
            movie.setDescription(movieDTO.getDescription());
            movie.setDurationMinutes(movieDTO.getDurationMinutes());
            movie.setGenre(movieDTO.getGenre());
            movie.setDirector(movieDTO.getDirector());
            movie.setCast(movieDTO.getCast());
            movie.setReleaseDate(movieDTO.getReleaseDate());
            movie.setPosterImageUrl(movieDTO.getPosterImageUrl());
            movie.setTrailerUrl(movieDTO.getTrailerUrl());
            movie.setRating(movieDTO.getRating());
            
            return convertToDTO(movieRepository.save(movie));
        });
    }

    @Transactional
    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }

    private MovieDTO convertToDTO(Movie movie) {
        MovieDTO dto = new MovieDTO();
        dto.setId(movie.getId());
        dto.setTitle(movie.getTitle());
        dto.setDescription(movie.getDescription());
        dto.setDurationMinutes(movie.getDurationMinutes());
        dto.setGenre(movie.getGenre());
        dto.setDirector(movie.getDirector());
        dto.setCast(movie.getCast());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setPosterImageUrl(movie.getPosterImageUrl());
        dto.setTrailerUrl(movie.getTrailerUrl());
        dto.setRating(movie.getRating());
        return dto;
    }

    private Movie convertToEntity(MovieDTO dto) {
        Movie movie = new Movie();
        // Don't set ID for new entities
        if (dto.getId() != null) {
            movie.setId(dto.getId());
        }
        movie.setTitle(dto.getTitle());
        movie.setDescription(dto.getDescription());
        movie.setDurationMinutes(dto.getDurationMinutes());
        movie.setGenre(dto.getGenre());
        movie.setDirector(dto.getDirector());
        movie.setCast(dto.getCast());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setPosterImageUrl(dto.getPosterImageUrl());
        movie.setTrailerUrl(dto.getTrailerUrl());
        movie.setRating(dto.getRating());
        return movie;
    }
    @Transactional(readOnly = true)
    public List<MovieDTO> getMoviesByIds(Collection<Long> movieIds) {
        if (movieIds == null || movieIds.isEmpty()) {
            return List.of();
        }

        return movieRepository.findAllById(movieIds).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}