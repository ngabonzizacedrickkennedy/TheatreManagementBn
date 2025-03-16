package com.thms.repository;

import com.thms.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDate;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByTitleContainingIgnoreCase(String title);
    
    List<Movie> findByGenre(Movie.Genre genre);
    
    List<Movie> findByReleaseDateAfter(LocalDate date);
    
    @Query("SELECT m FROM Movie m WHERE m.id IN (SELECT s.movie.id FROM Screening s WHERE s.theatre.id = :theatreId)")
    List<Movie> findMoviesByTheatreId(Long theatreId);
    
    @Query("SELECT m FROM Movie m WHERE m.id IN (SELECT s.movie.id FROM Screening s WHERE s.startTime >= CURRENT_DATE)")
    List<Movie> findCurrentlyPlayingMovies();
    
    @Query("SELECT m FROM Movie m WHERE m.releaseDate > CURRENT_DATE ORDER BY m.releaseDate ASC")
    List<Movie> findUpcomingMovies();
}