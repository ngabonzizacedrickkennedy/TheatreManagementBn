package com.thms.repository;

import com.thms.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserId(Long userId);
    
    List<Booking> findByScreeningId(Long screeningId);
    
    Optional<Booking> findByBookingNumber(String bookingNumber);
    
    List<Booking> findByUserIdAndBookingTimeAfter(Long userId, LocalDateTime date);
    
    @Query("SELECT b FROM Booking b WHERE b.screening.movie.id = :movieId")
    List<Booking> findByMovieId(Long movieId);
    
    @Query("SELECT b FROM Booking b WHERE b.screening.theatre.id = :theatreId")
    List<Booking> findByTheatreId(Long theatreId);
    
    @Query("SELECT DISTINCT bs FROM Booking b JOIN b.bookedSeats bs WHERE b.screening.id = :screeningId")
    List<String> findBookedSeatsByScreeningId(Long screeningId);
}