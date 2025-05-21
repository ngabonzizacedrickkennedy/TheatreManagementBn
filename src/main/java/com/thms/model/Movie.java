package com.thms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "movies")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String title;
    @Size(max = 500)
    private String description;
    @NotNull
    @PositiveOrZero
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    private Genre genre;

    @Size(max = 255)
    private String director;

    @Size(max = 255)
    @Column(name = "movie_cast")
    private String cast;

    private LocalDate releaseDate;

    @Column(name = "poster_image_url", columnDefinition = "TEXT")
    private String posterImageUrl;

    @Column(name = "trailer_url", columnDefinition = "TEXT")
    private String trailerUrl;

    @Enumerated(EnumType.STRING)
    private Rating rating;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL)
    private Set<Screening> screenings = new HashSet<>();

    public enum Genre {
        ACTION, ADVENTURE, ANIMATION, COMEDY, CRIME, DOCUMENTARY, DRAMA, FAMILY, 
        FANTASY, HORROR, MUSICAL, MYSTERY, ROMANCE, SCI_FI, THRILLER, WESTERN
    }

    public enum Rating {
        G, PG, PG13, R, NC17, UNRATED
    }
}

