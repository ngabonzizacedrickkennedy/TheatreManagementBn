package com.thms.repository;

import com.thms.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Long> {
    List<Theatre> findByNameContainingIgnoreCase(String name);

    List<Theatre> findByAddressContainingIgnoreCase(String address);

    @Query("SELECT t FROM Theatre t WHERE t.id IN (SELECT s.theatre.id FROM Screening s WHERE s.movie.id = :movieId)")
    List<Theatre> findTheatresByMovieId(Long movieId);

    // New search methods for global search
    List<Theatre> findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(String name, String address);

    @Query("SELECT t FROM Theatre t WHERE " +
            "LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.address) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(t.description) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Theatre> searchTheatres(@Param("query") String query);
}
