// GlobalSearchService.java
package com.thms.service;

import com.thms.dto.MovieDTO;
import com.thms.dto.ScreeningDTO;
import com.thms.dto.TheatreDTO;
import com.thms.dto.UserDTO;
import com.thms.model.Movie;
import com.thms.model.Screening;
import com.thms.model.Theatre;
import com.thms.model.User;
import com.thms.repository.MovieRepository;
import com.thms.repository.ScreeningRepository;
import com.thms.repository.TheatreRepository;
import com.thms.repository.UserRepository;
import com.thms.mapper.MovieMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class GlobalSearchService {

    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final ScreeningRepository screeningRepository;
    private final UserRepository userRepository;
    private final MovieMapper movieMapper;

    public GlobalSearchService(MovieRepository movieRepository,
                             TheatreRepository theatreRepository,
                             ScreeningRepository screeningRepository,
                             UserRepository userRepository,
                             MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
        this.screeningRepository = screeningRepository;
        this.userRepository = userRepository;
        this.movieMapper = movieMapper;
    }

    /**
     * Perform global search across all entities
     */
    public Map<String, Object> globalSearch(String query, int limit) {
        Map<String, Object> results = new HashMap<>();

        // Search movies
        List<MovieDTO> movies = searchMovieEntities(query, limit);
        results.put("movies", movies);

        // Search theatres
        List<TheatreDTO> theatres = searchTheatreEntities(query, limit);
        results.put("theatres", theatres);

        // Search screenings
        List<ScreeningDTO> screenings = searchScreeningEntities(query, limit);
        results.put("screenings", screenings);

        // Add metadata
        results.put("query", query);
        results.put("totalResults", movies.size() + theatres.size() + screenings.size());

        return results;
    }

    /**
     * Search movies only
     */
    public Map<String, Object> searchMovies(String query, int limit) {
        List<MovieDTO> movies = searchMovieEntities(query, limit);
        
        Map<String, Object> results = new HashMap<>();
        results.put("movies", movies);
        results.put("query", query);
        results.put("totalResults", movies.size());
        
        return results;
    }

    /**
     * Search theatres only
     */
    public Map<String, Object> searchTheatres(String query, int limit) {
        List<TheatreDTO> theatres = searchTheatreEntities(query, limit);
        
        Map<String, Object> results = new HashMap<>();
        results.put("theatres", theatres);
        results.put("query", query);
        results.put("totalResults", theatres.size());
        
        return results;
    }

    /**
     * Search screenings only
     */
    public Map<String, Object> searchScreenings(String query, int limit) {
        List<ScreeningDTO> screenings = searchScreeningEntities(query, limit);
        
        Map<String, Object> results = new HashMap<>();
        results.put("screenings", screenings);
        results.put("query", query);
        results.put("totalResults", screenings.size());
        
        return results;
    }

    /**
     * Search users only (Admin only)
     */
    public Map<String, Object> searchUsers(String query, int limit) {
        List<UserDTO> users = searchUserEntities(query, limit);
        
        Map<String, Object> results = new HashMap<>();
        results.put("users", users);
        results.put("query", query);
        results.put("totalResults", users.size());
        
        return results;
    }

    /**
     * Get search suggestions
     */
    public Map<String, Object> getSearchSuggestions(String query, int limit) {
        Map<String, Object> suggestions = new HashMap<>();
        
        // Movie title suggestions
        List<String> movieTitles = movieRepository.findByTitleContainingIgnoreCase(query)
                .stream()
                .limit(limit)
                .map(Movie::getTitle)
                .collect(Collectors.toList());
        
        // Theatre name suggestions
        List<String> theatreNames = theatreRepository.findByNameContainingIgnoreCase(query)
                .stream()
                .limit(limit)
                .map(Theatre::getName)
                .collect(Collectors.toList());
        
        suggestions.put("movieTitles", movieTitles);
        suggestions.put("theatreNames", theatreNames);
        suggestions.put("query", query);
        
        return suggestions;
    }

    // Private helper methods

    private List<MovieDTO> searchMovieEntities(String query, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        
        // Search by title, director, cast, or genre
        List<Movie> movies = movieRepository.findByTitleContainingIgnoreCaseOrDirectorContainingIgnoreCaseOrCastContainingIgnoreCase(
                query, query, query, pageable);
        
        return movies.stream()
                .map(movieMapper::toDTO)
                .collect(Collectors.toList());
    }

    private List<TheatreDTO> searchTheatreEntities(String query, int limit) {
        List<Theatre> theatres = theatreRepository.findByNameContainingIgnoreCaseOrAddressContainingIgnoreCase(
                query, query);
        
        return theatres.stream()
                .limit(limit)
                .map(this::convertTheatreToDTO)
                .collect(Collectors.toList());
    }

    private List<ScreeningDTO> searchScreeningEntities(String query, int limit) {
        LocalDateTime now = LocalDateTime.now();
        
        // Search screenings by movie title or theatre name (only future screenings)
        List<Screening> screenings = screeningRepository.findByMovieTitleOrTheatreNameAndStartTimeAfter(
                query, query, now);
        
        return screenings.stream()
                .limit(limit)
                .map(this::convertScreeningToDTO)
                .collect(Collectors.toList());
    }

    private List<UserDTO> searchUserEntities(String query, int limit) {
        List<User> users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                query, query, query, query);
        
        return users.stream()
                .limit(limit)
                .map(this::convertUserToDTO)
                .collect(Collectors.toList());
    }

    // DTO conversion methods
    private TheatreDTO convertTheatreToDTO(Theatre theatre) {
        TheatreDTO dto = new TheatreDTO();
        dto.setId(theatre.getId());
        dto.setName(theatre.getName());
        dto.setAddress(theatre.getAddress());
        dto.setPhoneNumber(theatre.getPhoneNumber());
        dto.setEmail(theatre.getEmail());
        dto.setDescription(theatre.getDescription());
        dto.setTotalScreens(theatre.getTotalScreens());
        dto.setImageUrl(theatre.getImageUrl());
        return dto;
    }

    private ScreeningDTO convertScreeningToDTO(Screening screening) {
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

    private UserDTO convertUserToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        // Don't include password in search results
        return dto;
    }
}
