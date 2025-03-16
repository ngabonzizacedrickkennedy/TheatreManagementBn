package com.thms.repository;

import com.thms.model.Screening;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ScreeningRepository extends JpaRepository<Screening, Long> {
    List<Screening> findByMovieId(Long movieId);
    
    List<Screening> findByTheatreId(Long theatreId);
    
    List<Screening> findByMovieIdAndTheatreId(Long movieId, Long theatreId);
    
    List<Screening> findByStartTimeAfterAndStartTimeBefore(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId AND s.theatre.id = :theatreId AND s.startTime >= :startDate ORDER BY s.startTime ASC")
    List<Screening> findAvailableScreenings(Long movieId, Long theatreId, LocalDateTime startDate);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.screening.id = :screeningId")
    Long countBookingsByScreeningId(Long screeningId);
}