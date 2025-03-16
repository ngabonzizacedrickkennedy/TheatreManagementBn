package com.thms.repository;

import com.thms.model.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByTheatreIdAndScreenNumber(Long theatreId, Integer screenNumber);
    
    List<Seat> findByTheatreIdAndScreenNumberAndSeatType(Long theatreId, Integer screenNumber, Seat.SeatType seatType);
    
    List<Seat> findByTheatreIdAndScreenNumberAndRowName(Long theatreId, Integer screenNumber, String rowName);
}