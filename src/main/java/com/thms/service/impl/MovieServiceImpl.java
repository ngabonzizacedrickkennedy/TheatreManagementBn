package com.thms.service.impl;

import com.thms.dto.MovieDTO;
import com.thms.mapper.MovieMapper;
import com.thms.model.Movie;
import com.thms.repository.MovieRepository;
import com.thms.service.MovieService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class MovieServiceImpl implements MovieService {

    private final MovieRepository movieRepository;
    private final MovieMapper movieMapper;

    public MovieServiceImpl(MovieRepository movieRepository, MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.movieMapper = movieMapper;
    }

    // Existing methods (keep all existing implementations)
    @Override
    @Transactional(readOnly = true)
    public List<MovieDTO> getAllMovies() {
        return movieRepository.findAll().stream()
                .map(movieMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MovieDTO> getMovieById(Long id) {
        return movieRepository.findById(id)
                .map(movieMapper::toDTO);
    }

    @Override
    public MovieDTO createMovie(MovieDTO movieDTO) {
        Movie movie = movieMapper.toEntity(movieDTO);
        Movie savedMovie = movieRepository.save(movie);
        return movieMapper.toDTO(savedMovie);
    }

    @Override
    public Optional<MovieDTO> updateMovie(Long id, MovieDTO movieDTO) {
        return movieRepository.findById(id)
                .map(existingMovie -> {
                    // Use mapper to update entity from DTO
                    movieMapper.updateEntityFromDTO(movieDTO, existingMovie);

                    Movie updatedMovie = movieRepository.save(existingMovie);
                    return movieMapper.toDTO(updatedMovie);
                });
    }

    @Override
    public void deleteMovie(Long id) {
        movieRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieDTO> searchMoviesByTitle(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(movieMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieDTO> getMoviesByGenre(Movie.Genre genre) {
        return movieRepository.findByGenre(genre).stream()
                .map(movieMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieDTO> getUpcomingMovies() {
        LocalDate currentDate = LocalDate.now();
        return movieRepository.findUpcomingMovies(currentDate).stream()
                .map(movieMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovieDTO> getMoviesByIds(Set<Long> ids) {
        return movieRepository.findAllById(ids).stream()
                .map(movieMapper::toDTO)
                .collect(Collectors.toList());
    }

    // New pagination methods
    @Override
    @Transactional(readOnly = true)
    public Page<MovieDTO> getAllMovies(Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findAll(pageable);
        List<MovieDTO> movieDTOs = moviePage.getContent().stream()
                .map(movieMapper::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(movieDTOs, pageable, moviePage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieDTO> searchMoviesByTitle(String title, Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findByTitleContainingIgnoreCase(title, pageable);
        List<MovieDTO> movieDTOs = moviePage.getContent().stream()
                .map(movieMapper::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(movieDTOs, pageable, moviePage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieDTO> getMoviesByGenre(Movie.Genre genre, Pageable pageable) {
        Page<Movie> moviePage = movieRepository.findByGenre(genre, pageable);
        List<MovieDTO> movieDTOs = moviePage.getContent().stream()
                .map(movieMapper::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(movieDTOs, pageable, moviePage.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MovieDTO> getUpcomingMovies(Pageable pageable) {
        LocalDate currentDate = LocalDate.now();
        Page<Movie> moviePage = movieRepository.findUpcomingMovies(currentDate, pageable);
        List<MovieDTO> movieDTOs = moviePage.getContent().stream()
                .map(movieMapper::toDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(movieDTOs, pageable, moviePage.getTotalElements());
    }

}