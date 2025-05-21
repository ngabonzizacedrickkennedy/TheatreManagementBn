package com.thms.dataloader;

import com.thms.dto.ApiResponse;
import com.thms.dto.MovieDTO;
import com.thms.dto.TheatreDTO;
import com.thms.dto.ScreeningDTO;
import com.thms.model.Movie;
import com.thms.model.Screening;
import com.thms.service.MovieService;
import com.thms.service.TheatreService;
import com.thms.service.ScreeningService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * API Controller for seeding the database with starter data
 * Provides endpoints to initialize movies, theatres, and screenings
 */
@RestController
@RequestMapping("/api/seed")
@RequiredArgsConstructor
public class DataSeederController {

    private final MovieService movieService;
    private final TheatreService theatreService;
    private final ScreeningService screeningService;

    /**
     * Seeds the database with sample movies
     * @return ResponseEntity with the operation result
     */
    @PostMapping("/movies")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedMovies() {
        List<MovieDTO> movies = getDefaultMovies();
        int count = 0;
        
        for (MovieDTO movie : movies) {
            try {
                movieService.createMovie(movie);
                count++;
            } catch (Exception e) {
                // Skip existing movies (based on title)
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("seeded", count);
        result.put("total", movies.size());
        
        return ResponseEntity.ok(ApiResponse.success(result, "Movies seeded successfully"));
    }

    /**
     * Seeds the database with sample theatres
     * @return ResponseEntity with the operation result
     */
    @PostMapping("/theatres")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedTheatres() {
        List<TheatreDTO> theatres = getDefaultTheatres();
        int count = 0;
        
        for (TheatreDTO theatre : theatres) {
            try {
                theatreService.createTheatre(theatre);
                count++;
            } catch (Exception e) {
                // Skip existing theatres (based on name)
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("seeded", count);
        result.put("total", theatres.size());
        
        return ResponseEntity.ok(ApiResponse.success(result, "Theatres seeded successfully"));
    }

    /**
     * Seeds the database with sample screenings
     * Creates screenings for existing movies and theatres
     * @return ResponseEntity with the operation result
     */
    @PostMapping("/screenings")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedScreenings() {
        // Get existing movies and theatres
        List<MovieDTO> movies = movieService.getAllMovies();
        List<TheatreDTO> theatres = theatreService.getAllTheatres();
        
        if (movies.isEmpty() || theatres.isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("Cannot create screenings: No movies or theatres exist")
            );
        }
        
        int count = 0;
        LocalDate today = LocalDate.now();
        
        // Create screenings for each movie in each theatre
        for (MovieDTO movie : movies) {
            for (TheatreDTO theatre : theatres) {
                // Skip if theatre doesn't have screens
                if (theatre.getTotalScreens() == null || theatre.getTotalScreens() < 1) {
                    continue;
                }
                
                // Create screenings for the next 7 days
                for (int day = 0; day < 7; day++) {
                    LocalDate screeningDate = today.plusDays(day);
                    
                    // Create 3 screenings per day (morning, afternoon, evening)
                    LocalTime[] times = {
                        LocalTime.of(10, 30), // 10:30 AM
                        LocalTime.of(14, 45), // 2:45 PM
                        LocalTime.of(19, 15)  // 7:15 PM
                    };
                    
                    for (int timeIndex = 0; timeIndex < times.length; timeIndex++) {
                        LocalTime screeningTime = times[timeIndex];
                        
                        // Create screening DTO
                        ScreeningDTO screening = new ScreeningDTO();
                        screening.setMovieId(movie.getId());
                        screening.setTheatreId(theatre.getId());
                        
                        // Set screen number (alternate between available screens)
                        int screenNumber = (timeIndex % theatre.getTotalScreens()) + 1;
                        screening.setScreenNumber(screenNumber);
                        
                        // Set format based on screen number (just for variety)
                        screening.setFormat(screenNumber % 2 == 0 ? Screening.ScreeningFormat.THREE_D : Screening.ScreeningFormat.STANDARD);
                        
                        // Set base price based on format and time of day
                        double basePrice = 10.00; // Standard price
                        if (screening.getFormat() == Screening.ScreeningFormat.THREE_D) {
                            basePrice += 3.00; // 3D surcharge
                        }
                        if (timeIndex == 2) {
                            basePrice += 2.00; // Evening surcharge
                        }
                        screening.setBasePrice(basePrice);
                        
                        // Set date and time
                        LocalDateTime startDateTime = LocalDateTime.of(screeningDate, screeningTime);
                        screening.setStartTime(startDateTime);
                        
                        // Set string representations for date/time fields
                        screening.setStartDateString(screeningDate.toString());
                        screening.setStartTimeString(screeningTime.toString());
                        
                        try {
                            screeningService.createScreening(screening);
                            count++;
                        } catch (Exception e) {
                            // Skip if a similar screening already exists
                        }
                    }
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("seeded", count);
        
        return ResponseEntity.ok(ApiResponse.success(result, "Screenings seeded successfully"));
    }
    
    /**
     * Seeds all data types at once (movies, theatres, and screenings)
     * @return ResponseEntity with the operation result
     */
    @PostMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> seedAll() {
        // Results container
        Map<String, Object> results = new HashMap<>();
        
        // Seed movies
        ResponseEntity<ApiResponse<Map<String, Object>>> moviesResponse = seedMovies();
        if (moviesResponse.getBody() != null && moviesResponse.getBody().getData() != null) {
            results.put("movies", moviesResponse.getBody().getData());
        }
        
        // Seed theatres
        ResponseEntity<ApiResponse<Map<String, Object>>> theatresResponse = seedTheatres();
        if (theatresResponse.getBody() != null && theatresResponse.getBody().getData() != null) {
            results.put("theatres", theatresResponse.getBody().getData());
        }
        
        // Seed screenings
        ResponseEntity<ApiResponse<Map<String, Object>>> screeningsResponse = seedScreenings();
        if (screeningsResponse.getBody() != null && screeningsResponse.getBody().getData() != null) {
            results.put("screenings", screeningsResponse.getBody().getData());
        }
        
        return ResponseEntity.ok(ApiResponse.success(results, "All data seeded successfully"));
    }

    /**
     * Get a list of default movies to seed
     * @return List of MovieDTO objects
     */
    private List<MovieDTO> getDefaultMovies() {
        return Arrays.asList(
            createMovie(
                "Inception", 
                "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O.",
                148, 
                Movie.Genre.SCI_FI,
                "Christopher Nolan",
                "Leonardo DiCaprio, Joseph Gordon-Levitt, Ellen Page, Tom Hardy",
                LocalDate.of(2010, 7, 16),
                "https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg", 
                "https://www.youtube.com/watch?v=YoHD9XEInc0",
                Movie.Rating.PG13
            ),
            createMovie(
                "The Shawshank Redemption",
                "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency.",
                142,
                Movie.Genre.DRAMA,
                "Frank Darabont",
                "Tim Robbins, Morgan Freeman, Bob Gunton",
                LocalDate.of(1994, 9, 23),
                "https://image.tmdb.org/t/p/w500/q6y0Go1tsGEsmtFryDOJo3dEmqu.jpg",
                "https://www.youtube.com/watch?v=6hB3S9bIaco",
                Movie.Rating.R
            ),
            createMovie(
                "The Dark Knight",
                "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.",
                152,
                Movie.Genre.ACTION,
                "Christopher Nolan",
                "Christian Bale, Heath Ledger, Aaron Eckhart",
                LocalDate.of(2008, 7, 18),
                "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
                "https://www.youtube.com/watch?v=EXeTwQWrcwY",
                Movie.Rating.PG13
            ),
            createMovie(
                "Pulp Fiction",
                "The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption.",
                154,
                Movie.Genre.CRIME,
                "Quentin Tarantino",
                "John Travolta, Samuel L. Jackson, Uma Thurman",
                LocalDate.of(1994, 10, 14),
                "https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg",
                "https://www.youtube.com/watch?v=s7EdQ4FqbhY",
                Movie.Rating.R
            ),
            createMovie(
                "The Matrix",
                "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.",
                136,
                Movie.Genre.SCI_FI,
                "Lana Wachowski, Lilly Wachowski",
                "Keanu Reeves, Laurence Fishburne, Carrie-Anne Moss",
                LocalDate.of(1999, 3, 31),
                "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg",
                "https://www.youtube.com/watch?v=vKQi3bBA1y8",
                Movie.Rating.R
            ),
            createMovie(
                "Toy Story 4",
                "When a new toy called Forky joins Woody and the gang, a road trip alongside old and new friends reveals how big the world can be for a toy.",
                100,
                Movie.Genre.ANIMATION,
                "Josh Cooley",
                "Tom Hanks, Tim Allen, Annie Potts",
                LocalDate.of(2019, 6, 21),
                "https://image.tmdb.org/t/p/w500/w9kR8qbmQ01HwnvK4alvnQ2ca0L.jpg",
                "https://www.youtube.com/watch?v=wmiIUN-7qhE",
                Movie.Rating.G
            ),
            createMovie(
                "The Lion King",
                "After the murder of his father, a young lion prince flees his kingdom only to learn the true meaning of responsibility and bravery.",
                118,
                Movie.Genre.ANIMATION,
                "Jon Favreau",
                "Donald Glover, Beyoncé, James Earl Jones",
                LocalDate.of(2019, 7, 19),
                "https://image.tmdb.org/t/p/w500/dzBtMocZuJbjLOXvrl4zGYigDzh.jpg",
                "https://www.youtube.com/watch?v=7TavVZMewpY",
                Movie.Rating.PG
            ),
            createMovie(
                "Interstellar",
                "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival.",
                169,
                Movie.Genre.SCI_FI,
                "Christopher Nolan",
                "Matthew McConaughey, Anne Hathaway, Jessica Chastain",
                LocalDate.of(2014, 11, 7),
                "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                "https://www.youtube.com/watch?v=zSWdZVtXT7E",
                Movie.Rating.PG13
            ),
            createMovie(
                "Avengers: Endgame",
                "After the devastating events of Avengers: Infinity War, the universe is in ruins. With the help of remaining allies, the Avengers assemble once more in order to reverse Thanos' actions and restore balance to the universe.",
                181,
                Movie.Genre.ACTION,
                "Anthony Russo, Joe Russo",
                "Robert Downey Jr., Chris Evans, Mark Ruffalo",
                LocalDate.of(2019, 4, 26),
                "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg",
                "https://www.youtube.com/watch?v=TcMBFSGVi1c",
                Movie.Rating.PG13
            ),
            createMovie(
                "The Silence of the Lambs",
                "A young F.B.I. cadet must receive the help of an incarcerated and manipulative cannibal killer to help catch another serial killer, a madman who skins his victims.",
                118,
                Movie.Genre.THRILLER,
                "Jonathan Demme",
                "Jodie Foster, Anthony Hopkins, Scott Glenn",
                LocalDate.of(1991, 2, 14),
                "https://image.tmdb.org/t/p/w500/rplLJ2hPcOQmkFhTqUte0Oi9qZz.jpg",
                "https://www.youtube.com/watch?v=W6Mm8Sbe__o",
                Movie.Rating.R
            )
        );
    }

    /**
     * Helper method to create a MovieDTO
     */
    private MovieDTO createMovie(String title, String description, int durationMinutes, Movie.Genre genre, 
                                 String director, String cast, LocalDate releaseDate, String posterImageUrl, 
                                 String trailerUrl, Movie.Rating rating) {
        MovieDTO movie = new MovieDTO();
        movie.setTitle(title);
        movie.setDescription(description);
        movie.setDurationMinutes(durationMinutes);
        movie.setGenre(genre);
        movie.setDirector(director);
        movie.setCast(cast);
        movie.setReleaseDate(releaseDate);
        movie.setPosterImageUrl(posterImageUrl);
        movie.setTrailerUrl(trailerUrl);
        movie.setRating(rating);
        return movie;
    }

    /**
     * Get a list of default theatres to seed
     * @return List of TheatreDTO objects
     */
    private List<TheatreDTO> getDefaultTheatres() {
        return Arrays.asList(
            createTheatre(
                "Cineplex Downtown",
                "123 Main Street, Downtown, NYC 10001",
                "+1 (212) 555-1234",
                "info@cineplexdowntown.com",
                "Experience the ultimate in movie entertainment at our flagship theatre in the heart of downtown. Featuring state-of-the-art projection and sound systems in all screens.",
                8,
                "https://images.unsplash.com/photo-1517604931442-7e0c8ed2963c?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=800&q=80"
            ),
            createTheatre(
                "Westside Cinema",
                "456 West Avenue, Westside, NYC 10023",
                "+1 (212) 555-5678",
                "info@westsidecinema.com",
                "Our newest location featuring IMAX screens and luxury seating in all auditoriums. Enjoy gourmet dining options and a full-service bar in our lounge area.",
                6,
                "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=800&q=80"
            ),
            createTheatre(
                "Eastside Movie House",
                "789 East Blvd, Eastside, NYC 10028",
                "+1 (212) 555-9012",
                "contact@eastsidemovies.com",
                "A historic theatre renovated with modern amenities while preserving its classic charm. Showing a mix of mainstream and independent films in a cozy setting.",
                4,
                "https://images.unsplash.com/photo-1594909122845-11baa439b7bf?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=800&q=80"
            ),
            createTheatre(
                "Uptown Screens",
                "101 North Street, Uptown, NYC 10029",
                "+1 (212) 555-3456",
                "hello@uptownscreens.com",
                "Family-friendly theatre with special weekend programs for children. Featuring comfortable seating and affordable concessions in a convenient uptown location.",
                5,
                "https://images.unsplash.com/photo-1615120943946-f05b59b9e0b6?ixlib=rb-4.0.3&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=800&q=80"
            )
        );
    }

    /**
     * Helper method to create a TheatreDTO
     */
    private TheatreDTO createTheatre(String name, String address, String phoneNumber, String email, String description, 
                                    Integer totalScreens, String imageUrl) {
        TheatreDTO theatre = new TheatreDTO();
        theatre.setName(name);
        theatre.setAddress(address);
        theatre.setPhoneNumber(phoneNumber);
        theatre.setEmail(email);
        theatre.setDescription(description);
        theatre.setTotalScreens(totalScreens);
        theatre.setImageUrl(imageUrl);
        return theatre;
    }
}