package com.thms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "theatres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Theatre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Size(max = 200)
    private String address;

    @Size(max = 20)
    private String phoneNumber;

    @Size(max = 100)
    private String email;

    @Size(max = 500)
    private String description;

    @PositiveOrZero
    private Integer totalScreens;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @OneToMany(mappedBy = "theatre", cascade = CascadeType.ALL)
    private Set<Screening> screenings = new HashSet<>();
    
    @OneToMany(mappedBy = "theatre", cascade = CascadeType.ALL)
    private Set<Seat> seats = new HashSet<>();
}