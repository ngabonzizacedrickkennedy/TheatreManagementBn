package com.thms.service;

import com.thms.model.Seat;
import com.thms.model.Theatre;
import com.thms.repository.SeatRepository;
import com.thms.repository.TheatreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatService {

    private final SeatRepository seatRepository;
    private final TheatreRepository theatreRepository;

    public SeatService(SeatRepository seatRepository, TheatreRepository theatreRepository) {
        this.seatRepository = seatRepository;
        this.theatreRepository = theatreRepository;
    }

    @Transactional(readOnly = true)
    public Optional<Seat> getSeatById(Long id) {
        return seatRepository.findById(id);
    }

    @Transactional
    public void initializeSeatsForTheatre(Long theatreId, Integer screenNumber, int rows, int seatsPerRow) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new RuntimeException("Theatre not found with id: " + theatreId));

        // Check if seats already exist for this screen
        if (!seatRepository.findByTheatreIdAndScreenNumber(theatreId, screenNumber).isEmpty()) {
            throw new RuntimeException("Seats already exist for this screen. Delete them first before reinitializing.");
        }

        List<Seat> seats = new ArrayList<>();

        for (int row = 0; row < rows; row++) {
            // Convert row number to letter (A, B, C, ...)
            String rowName = String.valueOf((char) ('A' + row));

            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                Seat seat = new Seat();
                seat.setTheatre(theatre);
                seat.setScreenNumber(screenNumber);
                seat.setRowName(rowName);
                seat.setSeatNumber(seatNum);

                // Assign seat types based on position
                if (row < 2) {
                    // Front rows are standard
                    seat.setSeatType(Seat.SeatType.STANDARD);
                    seat.setPriceMultiplier(1.0);
                } else if (row >= 2 && row < rows - 2) {
                    // Middle rows are premium
                    seat.setSeatType(Seat.SeatType.PREMIUM);
                    seat.setPriceMultiplier(1.2);
                } else {
                    // Back rows are VIP
                    seat.setSeatType(Seat.SeatType.VIP);
                    seat.setPriceMultiplier(1.5);
                }

                // Mark some seats as accessible
                if (row == rows / 2 && (seatNum == 1 || seatNum == seatsPerRow)) {
                    seat.setSeatType(Seat.SeatType.ACCESSIBLE);
                    seat.setPriceMultiplier(1.0);
                }

                seats.add(seat);
            }
        }

        seatRepository.saveAll(seats);
    }

    @Transactional(readOnly = true)
    public List<Seat> getSeatsByTheatreAndScreen(Long theatreId, Integer screenNumber) {
        return seatRepository.findByTheatreIdAndScreenNumber(theatreId, screenNumber);
    }

    @Transactional(readOnly = true)
    public Map<String, List<Seat>> getSeatMapByTheatreAndScreen(Long theatreId, Integer screenNumber) {
        // Group seats by row for easier display
        return seatRepository.findByTheatreIdAndScreenNumber(theatreId, screenNumber).stream()
                .collect(Collectors.groupingBy(Seat::getRowName,
                        Collectors.mapping(seat -> seat, Collectors.toList())))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .sorted(Comparator.comparing(Seat::getSeatNumber))
                                .collect(Collectors.toList()),
                        (e1, e2) -> e1,
                        TreeMap::new));  // Use TreeMap to sort by row name
    }

    @Transactional(readOnly = true)
    public List<Seat> getSeatsByType(Long theatreId, Integer screenNumber, Seat.SeatType seatType) {
        return seatRepository.findByTheatreIdAndScreenNumberAndSeatType(theatreId, screenNumber, seatType);
    }

    @Transactional
    public void updateSeatType(Long seatId, Seat.SeatType seatType, Double priceMultiplier) {
        seatRepository.findById(seatId).ifPresent(seat -> {
            seat.setSeatType(seatType);
            seat.setPriceMultiplier(priceMultiplier);
            seatRepository.save(seat);
        });
    }

    @Transactional
    public void updateSeatRowType(Long theatreId, Integer screenNumber, String rowName,
                                  Seat.SeatType seatType, Double priceMultiplier) {
        List<Seat> rowSeats = seatRepository.findByTheatreIdAndScreenNumberAndRowName(theatreId, screenNumber, rowName);

        if (rowSeats.isEmpty()) {
            throw new RuntimeException("No seats found for the specified row");
        }

        rowSeats.forEach(seat -> {
            seat.setSeatType(seatType);
            seat.setPriceMultiplier(priceMultiplier);
        });

        seatRepository.saveAll(rowSeats);
    }

    @Transactional
    public int bulkUpdateSeats(List<String> seatIds, Long theatreId, Integer screenNumber,
                               Seat.SeatType seatType, Double priceMultiplier) {
        List<Seat> seats = seatRepository.findByTheatreIdAndScreenNumber(theatreId, screenNumber);

        List<Seat> seatsToUpdate = seats.stream()
                .filter(seat -> seatIds.contains(seat.getRowName() + seat.getSeatNumber()))
                .collect(Collectors.toList());

        seatsToUpdate.forEach(seat -> {
            seat.setSeatType(seatType);
            seat.setPriceMultiplier(priceMultiplier);
        });

        seatRepository.saveAll(seatsToUpdate);
        return seatsToUpdate.size();
    }

    @Transactional
    public int deleteScreenSeats(Long theatreId, Integer screenNumber) {
        List<Seat> seats = seatRepository.findByTheatreIdAndScreenNumber(theatreId, screenNumber);
        int count = seats.size();

        seatRepository.deleteAll(seats);
        return count;
    }
}