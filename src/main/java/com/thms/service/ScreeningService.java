package com.thms.service;

import com.thms.dto.ScreeningDTO;
import com.thms.exception.ResourceNotFoundException;
import com.thms.model.Booking;
import com.thms.model.Movie;
import com.thms.model.Screening;
import com.thms.model.Theatre;
import com.thms.repository.BookingRepository;
import com.thms.repository.MovieRepository;
import com.thms.repository.ScreeningRepository;
import com.thms.repository.TheatreRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ScreeningService implements IScreeningService {

    private final ScreeningRepository screeningRepository;
    private final BookingRepository bookingRepository;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;

    public ScreeningService(ScreeningRepository screeningRepository,
                            BookingRepository bookingRepository, MovieRepository movieRepository,
                            TheatreRepository theatreRepository) {
        this.screeningRepository = screeningRepository;
        this.bookingRepository = bookingRepository;
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
    }

    @Transactional
    public ScreeningDTO createScreening(ScreeningDTO screeningDTO) {
        Movie movie = movieRepository.findById(screeningDTO.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found with id: " + screeningDTO.getMovieId()));

        Theatre theatre = theatreRepository.findById(screeningDTO.getTheatreId())
                .orElseThrow(() -> new RuntimeException("Theatre not found with id: " + screeningDTO.getTheatreId()));

        // Calculate end time based on movie duration
        LocalDateTime endTime = screeningDTO.getStartTime().plusMinutes(movie.getDurationMinutes());

        // Check for scheduling conflicts
        boolean hasConflict = screeningRepository.findByTheatreId(theatre.getId()).stream()
                .anyMatch(s ->
                        s.getScreenNumber().equals(screeningDTO.getScreenNumber()) &&
                                s.getStartTime().isBefore(endTime) &&
                                s.getEndTime().isAfter(screeningDTO.getStartTime())
                );

        if (hasConflict) {
            throw new RuntimeException("There is a scheduling conflict with another screening");
        }

        Screening screening = new Screening();
        screening.setMovie(movie);
        screening.setTheatre(theatre);
        screening.setStartTime(screeningDTO.getStartTime());
        screening.setEndTime(endTime);
        screening.setScreenNumber(screeningDTO.getScreenNumber());
        screening.setFormat(screeningDTO.getFormat());
        screening.setBasePrice(screeningDTO.getBasePrice());

        Screening savedScreening = screeningRepository.save(screening);
        return convertToDTO(savedScreening);
    }

    @Transactional(readOnly = true)
    public List<ScreeningDTO> getAllScreenings() {
        return screeningRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScreeningDTO> getScreenings(Long movieId, Long theatreId, LocalDate date) {
        List<Screening> screenings;

        if (movieId != null && theatreId != null && date != null) {
            // Filter by movie, theatre and date
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            screenings = screeningRepository.findByMovieIdAndTheatreIdAndStartTimeBetween(
                    movieId, theatreId, startOfDay, endOfDay);
        } else if (movieId != null && theatreId != null) {
            // Filter by movie and theatre
            screenings = screeningRepository.findByMovieIdAndTheatreId(movieId, theatreId);
        } else if (movieId != null && date != null) {
            // Filter by movie and date
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            screenings = screeningRepository.findByMovieIdAndStartTimeBetween(
                    movieId, startOfDay, endOfDay);
        } else if (theatreId != null && date != null) {
            // Filter by theatre and date
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            screenings = screeningRepository.findByTheatreIdAndStartTimeBetween(
                    theatreId, startOfDay, endOfDay);
        } else if (movieId != null) {
            // Filter by movie
            screenings = screeningRepository.findByMovieId(movieId);
        } else if (theatreId != null) {
            // Filter by theatre
            screenings = screeningRepository.findByTheatreId(theatreId);
        } else if (date != null) {
            // Filter by date
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            screenings = screeningRepository.findByStartTimeBetween(startOfDay, endOfDay);
        } else {
            // No filters, return all future screenings
            screenings = screeningRepository.findByStartTimeAfter(LocalDateTime.now());
        }

        return screenings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ScreeningDTO> getScreeningById(Long id) {
        return screeningRepository.findById(id).map(this::convertToDTO);
    }

    @Override
    public List<ScreeningDTO> getScreeningsByMovie(Long movieId, Integer days) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Movie not found with id: " + movieId));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endDate = now.plusDays(days);

        List<Screening> screenings = screeningRepository.findByMovieIdAndStartTimeBetween(
                movieId, now, endDate);

        return screenings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ScreeningDTO> getScreeningsByTheatre(Long theatreId, LocalDate date) {
        Theatre theatre = theatreRepository.findById(theatreId)
                .orElseThrow(() -> new ResourceNotFoundException("Theatre not found with id: " + theatreId));

        List<Screening> screenings;

        if (date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

            screenings = screeningRepository.findByTheatreIdAndStartTimeBetween(
                    theatreId, startOfDay, endOfDay);
        } else {
            // If no date specified, return upcoming screenings for this theatre
            LocalDateTime now = LocalDateTime.now();
            screenings = screeningRepository.findByTheatreIdAndStartTimeAfter(theatreId, now);
        }

        return screenings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, List<ScreeningDTO>> getUpcomingScreenings(Integer days) {
        // Get current date and time
        LocalDateTime now = LocalDateTime.now();

        // Calculate end date (now + days)
        LocalDateTime endDate = now.plusDays(days);

        // Find all screenings between now and end date
        List<Screening> screenings = screeningRepository.findByStartTimeBetweenOrderByStartTimeAsc(now, endDate);

        // Convert to DTOs
        List<ScreeningDTO> screeningDTOs = screenings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        // Group screenings by date
        Map<String, List<ScreeningDTO>> screeningsByDate = new HashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (ScreeningDTO screening : screeningDTOs) {
            String dateKey = screening.getStartTime().format(dateFormatter);

            if (!screeningsByDate.containsKey(dateKey)) {
                screeningsByDate.put(dateKey, new ArrayList<>());
            }

            screeningsByDate.get(dateKey).add(screening);
        }

        return screeningsByDate;
    }

    @Override
    public List<ScreeningDTO> getScreeningsByDateRange(LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        List<Screening> screenings = screeningRepository.findByStartTimeBetween(startDateTime, endDateTime);

        return screenings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> getAvailableSeats(Long screeningId) {
        // Get all seats for the theatre's screen
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + screeningId));

        // This would need actual implementation based on your data model
        // For now, returning a dummy implementation

        // Get all possible seats (e.g., A1-J10 for a 10x10 theatre)
        Set<String> allSeats = generateAllSeatsForScreen(screening);

        // Get booked seats
        Set<String> bookedSeats = getBookedSeats(screeningId);

        // Remove booked seats from all seats to get available seats
        allSeats.removeAll(bookedSeats);

        return allSeats;
    }

    @Override
    public Set<String> getBookedSeats(Long screeningId) {
        // Find all bookings for this screening
        List<Booking> bookings = bookingRepository.findByScreeningId(screeningId);

        // Collect all booked seats
        return bookings.stream()
                .filter(booking -> booking.getPaymentStatus() != Booking.PaymentStatus.CANCELLED)
                .flatMap(booking -> booking.getBookedSeats().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Object getSeatingLayout(Long screeningId) {
        Screening screening = screeningRepository.findById(screeningId)
                .orElseThrow(() -> new ResourceNotFoundException("Screening not found with id: " + screeningId));

        // This would need actual implementation based on your data model
        // For now, returning a dummy implementation with row information and prices

        // Get the theatre and screen information
        Theatre theatre = screening.getTheatre();

        // Create a simple layout with rows, seat counts, and price multipliers
        Map<String, Object> layout = new HashMap<>();
        layout.put("basePrice", screening.getBasePrice());

        // Define some example rows with different price tiers
        List<Map<String, Object>> rows = new ArrayList<>();

        // Standard rows (A-E)
        for (char rowChar = 'A'; rowChar <= 'E'; rowChar++) {
            Map<String, Object> row = new HashMap<>();
            row.put("name", String.valueOf(rowChar));
            row.put("seatsCount", 10);
            row.put("seatType", "STANDARD");
            row.put("priceMultiplier", 1.0);
            rows.add(row);
        }

        // Premium rows (F-H)
        for (char rowChar = 'F'; rowChar <= 'H'; rowChar++) {
            Map<String, Object> row = new HashMap<>();
            row.put("name", String.valueOf(rowChar));
            row.put("seatsCount", 10);
            row.put("seatType", "PREMIUM");
            row.put("priceMultiplier", 1.5);
            rows.add(row);
        }

        // VIP row (I)
        Map<String, Object> vipRow = new HashMap<>();
        vipRow.put("name", "I");
        vipRow.put("seatsCount", 8);
        vipRow.put("seatType", "VIP");
        vipRow.put("priceMultiplier", 2.0);
        rows.add(vipRow);

        // Wheelchair accessible row (J)
        Map<String, Object> accessibleRow = new HashMap<>();
        accessibleRow.put("name", "J");
        accessibleRow.put("seatsCount", 6);
        accessibleRow.put("seatType", "WHEELCHAIR");
        accessibleRow.put("priceMultiplier", 1.0);
        rows.add(accessibleRow);

        layout.put("rows", rows);

        return layout;
    }
    private Set<String> generateAllSeatsForScreen(Screening screening) {
        Set<String> allSeats = new HashSet<>();

        // Generate A1-J10 seats for demo purposes
        for (char row = 'A'; row <= 'J'; row++) {
            for (int seatNum = 1; seatNum <= 10; seatNum++) {
                allSeats.add(row + String.valueOf(seatNum));
            }
        }

        return allSeats;
    }

    @Transactional(readOnly = true)
    public Optional<Screening> getScreeningEntityById(Long id) {
        return screeningRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<ScreeningDTO> getScreeningsByMovie(Long movieId) {
        return screeningRepository.findByMovieId(movieId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScreeningDTO> getScreeningsByTheatre(Long theatreId) {
        return screeningRepository.findByTheatreId(theatreId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScreeningDTO> getScreeningsByMovieAndTheatre(Long movieId, Long theatreId) {
        return screeningRepository.findByMovieIdAndTheatreId(movieId, theatreId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScreeningDTO> getScreeningsByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return screeningRepository.findByStartTimeAfterAndStartTimeBefore(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ScreeningDTO> getAvailableScreenings(Long movieId, Long theatreId, LocalDateTime startDate) {
        return screeningRepository.findAvailableScreenings(movieId, theatreId, startDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<ScreeningDTO> updateScreening(Long id, ScreeningDTO screeningDTO) {
        return screeningRepository.findById(id).map(screening -> {
            // Don't change movie and theatre for existing screenings
            screening.setStartTime(screeningDTO.getStartTime());

            // Recalculate end time based on the movie duration
            screening.setEndTime(screeningDTO.getStartTime()
                    .plusMinutes(screening.getMovie().getDurationMinutes()));

            screening.setScreenNumber(screeningDTO.getScreenNumber());
            screening.setFormat(screeningDTO.getFormat());
            screening.setBasePrice(screeningDTO.getBasePrice());

            return convertToDTO(screeningRepository.save(screening));
        });
    }

    @Transactional
    public void deleteScreening(Long id) {
        screeningRepository.deleteById(id);
    }

    private ScreeningDTO convertToDTO(Screening screening) {
        ScreeningDTO dto = new ScreeningDTO();
        dto.setId(screening.getId());
        dto.setMovieId(screening.getMovie().getId());
        dto.setMovieTitle(screening.getMovie().getTitle());
        dto.setTheatreId(screening.getTheatre().getId());
        dto.setTheatreName(screening.getTheatre().getName());
        dto.setStartTime(screening.getStartTime());
        dto.setEndTime(screening.getEndTime());
        dto.setScreenNumber(screening.getScreenNumber());
        dto.setFormat(screening.getFormat());
        dto.setBasePrice(screening.getBasePrice());
        return dto;
    }
    @Transactional(readOnly = true)
    public List<ScreeningDTO> getUpcomingScreenings(LocalDateTime fromDateTime) {
        return screeningRepository.findByStartTimeAfterOrderByStartTimeAsc(fromDateTime).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    @Override
    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getAllScreenings(Pageable pageable) {
        Page<Screening> screeningPage = screeningRepository.findAll(pageable);
        return screeningPage.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getScreenings(Long movieId, Long theatreId, LocalDate date, Pageable pageable) {
        Page<Screening> screeningPage;

        if (movieId != null && theatreId != null && date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            screeningPage = screeningRepository.findByMovieIdAndTheatreIdAndStartTimeBetween(
                    movieId, theatreId, startOfDay, endOfDay, pageable);
        } else if (movieId != null && theatreId != null) {
            screeningPage = screeningRepository.findByMovieIdAndTheatreId(movieId, theatreId, pageable);
        } else if (movieId != null && date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            screeningPage = screeningRepository.findByMovieIdAndStartTimeBetween(
                    movieId, startOfDay, endOfDay, pageable);
        } else if (theatreId != null && date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            screeningPage = screeningRepository.findByTheatreIdAndStartTimeBetween(
                    theatreId, startOfDay, endOfDay, pageable);
        } else if (movieId != null) {
            screeningPage = screeningRepository.findByMovieId(movieId, pageable);
        } else if (theatreId != null) {
            screeningPage = screeningRepository.findByTheatreId(theatreId, pageable);
        } else if (date != null) {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
            screeningPage = screeningRepository.findByStartTimeBetween(startOfDay, endOfDay, pageable);
        } else {
            screeningPage = screeningRepository.findByStartTimeAfter(LocalDateTime.now(), pageable);
        }

        return screeningPage.map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getScreeningsByMovie(Long movieId, Pageable pageable) {
        return screeningRepository.findByMovieId(movieId, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getScreeningsByTheatre(Long theatreId, Pageable pageable) {
        return screeningRepository.findByTheatreId(theatreId, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getScreeningsByMovieAndTheatre(Long movieId, Long theatreId, Pageable pageable) {
        return screeningRepository.findByMovieIdAndTheatreId(movieId, theatreId, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getScreeningsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return screeningRepository.findByStartTimeBetween(startDate, endDate, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getAvailableScreenings(Long movieId, Long theatreId, LocalDateTime startDate, Pageable pageable) {
        return screeningRepository.findAvailableScreenings(movieId, theatreId, startDate, pageable).map(this::convertToDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ScreeningDTO> getUpcomingScreenings(LocalDateTime fromDateTime, Pageable pageable) {
        return screeningRepository.findByStartTimeAfter(fromDateTime, pageable).map(this::convertToDTO);
    }

}