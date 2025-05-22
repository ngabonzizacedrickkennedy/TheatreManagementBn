package com.thms.mapper;

import com.thms.dto.MovieDTO;
import com.thms.model.Movie;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between Movie entity and MovieDTO
 */
@Component
public class MovieMapper {

    /**
     * Convert Movie entity to MovieDTO
     * @param movie Movie entity
     * @return MovieDTO
     */
    public MovieDTO toDTO(Movie movie) {
        if (movie == null) {
            return null;
        }

        MovieDTO movieDTO = new MovieDTO();
        movieDTO.setId(movie.getId());
        movieDTO.setTitle(movie.getTitle());
        movieDTO.setDescription(movie.getDescription());
        movieDTO.setDurationMinutes(movie.getDurationMinutes());
        movieDTO.setGenre(movie.getGenre());
        movieDTO.setDirector(movie.getDirector());
        movieDTO.setCast(movie.getCast());
        movieDTO.setReleaseDate(movie.getReleaseDate());
        movieDTO.setPosterImageUrl(movie.getPosterImageUrl());
        movieDTO.setTrailerUrl(movie.getTrailerUrl());
        movieDTO.setRating(movie.getRating());

        return movieDTO;
    }

    /**
     * Convert MovieDTO to Movie entity
     * @param movieDTO MovieDTO
     * @return Movie entity
     */
    public Movie toEntity(MovieDTO movieDTO) {
        if (movieDTO == null) {
            return null;
        }

        Movie movie = new Movie();
        movie.setId(movieDTO.getId());
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

        return movie;
    }

    /**
     * Update existing Movie entity with data from MovieDTO
     * @param movieDTO Source DTO
     * @param movie Target entity to update
     */
    public void updateEntityFromDTO(MovieDTO movieDTO, Movie movie) {
        if (movieDTO == null || movie == null) {
            return;
        }

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
    }
}