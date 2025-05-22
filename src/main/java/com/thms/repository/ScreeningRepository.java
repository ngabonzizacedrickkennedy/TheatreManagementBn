package com.thms.repository;

import com.thms.model.Screening;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    List<Screening> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime startTime);
    /**
     * Find screenings by start time between ordered by start time
     */
    List<Screening> findByStartTimeBetweenOrderByStartTimeAsc(LocalDateTime startTime, LocalDateTime endTime);

    List<Screening> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    List<Screening> findByTheatreIdAndStartTimeAfter(Long theatreId, LocalDateTime startTime);

    List<Screening> findByTheatreIdAndStartTimeBetween(Long theatreId, LocalDateTime starTime, LocalDateTime endTime);

    List<Screening> findByMovieIdAndStartTimeBetween(Long movieId, LocalDateTime startTime, LocalDateTime endTime);

    List<Screening> findByMovieIdAndTheatreIdAndStartTimeBetween(Long movieId, Long theatreId, LocalDateTime startTime, LocalDateTime endTime);

    List<Screening> findByStartTimeAfter(LocalDateTime starTime);
    Page<Screening> findByMovieId(Long movieId, Pageable pageable);
    Page<Screening> findByTheatreId(Long theatreId, Pageable pageable);
    Page<Screening> findByMovieIdAndTheatreId(Long movieId, Long theatreId, Pageable pageable);
    Page<Screening> findByStartTimeAfterAndStartTimeBefore(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Screening> findByStartTimeAfter(LocalDateTime startTime, Pageable pageable);
    Page<Screening> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    Page<Screening> findByTheatreIdAndStartTimeAfter(Long theatreId, LocalDateTime startTime, Pageable pageable);
    Page<Screening> findByTheatreIdAndStartTimeBetween(Long theatreId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    Page<Screening> findByMovieIdAndStartTimeBetween(Long movieId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    Page<Screening> findByMovieIdAndTheatreIdAndStartTimeBetween(Long movieId, Long theatreId, LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);

    @Query("SELECT s FROM Screening s WHERE s.movie.id = :movieId AND s.theatre.id = :theatreId AND s.startTime >= :startDate ORDER BY s.startTime ASC")
    Page<Screening> findAvailableScreenings(@Param("movieId") Long movieId,
                                            @Param("theatreId") Long theatreId,
                                            @Param("startDate") LocalDateTime startDate,
                                            Pageable pageable);
}