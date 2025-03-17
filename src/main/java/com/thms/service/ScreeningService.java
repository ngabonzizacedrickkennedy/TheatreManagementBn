package com.thms.service;

import com.thms.dto.ScreeningDTO;
import com.thms.model.Movie;
import com.thms.model.Screening;
import com.thms.model.Theatre;
import com.thms.repository.MovieRepository;
import com.thms.repository.ScreeningRepository;
import com.thms.repository.TheatreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ScreeningService {

    private final ScreeningRepository screeningRepository;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;

    public ScreeningService(ScreeningRepository screeningRepository,
                            MovieRepository movieRepository,
                            TheatreRepository theatreRepository) {
        this.screeningRepository = screeningRepository;
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

    @Transactional(readOnly = true)
    public Optional<ScreeningDTO> getScreeningById(Long id) {
        return screeningRepository.findById(id).map(this::convertToDTO);
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
}