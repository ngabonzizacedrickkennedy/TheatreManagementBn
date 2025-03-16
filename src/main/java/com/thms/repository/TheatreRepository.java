package com.thms.repository;

import com.thms.model.Theatre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TheatreRepository extends JpaRepository<Theatre, Long> {
    List<Theatre> findByNameContainingIgnoreCase(String name);
    
    List<Theatre> findByAddressContainingIgnoreCase(String address);
    
    @Query("SELECT t FROM Theatre t WHERE t.id IN (SELECT s.theatre.id FROM Screening s WHERE s.movie.id = :movieId)")
    List<Theatre> findTheatresByMovieId(Long movieId);
}