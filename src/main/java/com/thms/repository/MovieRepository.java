package com.thms.repository;

import com.thms.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    // Existing methods
    List<Movie> findByTitleContainingIgnoreCase(String title);
    List<Movie> findByGenre(Movie.Genre genre);
    List<Movie> findByReleaseDateAfter(LocalDate date);

    @Query("SELECT m FROM Movie m WHERE m.releaseDate > :currentDate ORDER BY m.releaseDate ASC")
    List<Movie> findUpcomingMovies(@Param("currentDate") LocalDate currentDate);

    // New pagination methods
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    Page<Movie> findByGenre(Movie.Genre genre, Pageable pageable);
    Page<Movie> findByReleaseDateAfter(LocalDate date, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.releaseDate > :currentDate")
    Page<Movie> findUpcomingMovies(@Param("currentDate") LocalDate currentDate, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE " +
            "(:title IS NULL OR LOWER(m.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:genre IS NULL OR m.genre = :genre)")
    Page<Movie> findMoviesWithFilters(@Param("title") String title,
                                      @Param("genre") Movie.Genre genre,
                                      Pageable pageable);
}