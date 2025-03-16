package com.thms.service;

import com.thms.dto.TheatreDTO;
import com.thms.model.Theatre;
import com.thms.repository.TheatreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TheatreService {

    private final TheatreRepository theatreRepository;

    public TheatreService(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    @Transactional
    public TheatreDTO createTheatre(TheatreDTO theatreDTO) {
        Theatre theatre = convertToEntity(theatreDTO);
        Theatre savedTheatre = theatreRepository.save(theatre);
        return convertToDTO(savedTheatre);
    }

    @Transactional(readOnly = true)
    public List<TheatreDTO> getAllTheatres() {
        return theatreRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<TheatreDTO> getTheatreById(Long id) {
        return theatreRepository.findById(id).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<TheatreDTO> searchTheatresByName(String name) {
        return theatreRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TheatreDTO> searchTheatresByAddress(String address) {
        return theatreRepository.findByAddressContainingIgnoreCase(address).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TheatreDTO> getTheatresByMovie(Long movieId) {
        return theatreRepository.findTheatresByMovieId(movieId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public Optional<TheatreDTO> updateTheatre(Long id, TheatreDTO theatreDTO) {
        return theatreRepository.findById(id).map(theatre -> {
            theatre.setName(theatreDTO.getName());
            theatre.setAddress(theatreDTO.getAddress());
            theatre.setPhoneNumber(theatreDTO.getPhoneNumber());
            theatre.setEmail(theatreDTO.getEmail());
            theatre.setDescription(theatreDTO.getDescription());
            theatre.setTotalScreens(theatreDTO.getTotalScreens());
            theatre.setImageUrl(theatreDTO.getImageUrl());
            
            return convertToDTO(theatreRepository.save(theatre));
        });
    }

    @Transactional
    public void deleteTheatre(Long id) {
        theatreRepository.deleteById(id);
    }

    private TheatreDTO convertToDTO(Theatre theatre) {
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

    private Theatre convertToEntity(TheatreDTO dto) {
        Theatre theatre = new Theatre();
        // Don't set ID for new entities
        if (dto.getId() != null) {
            theatre.setId(dto.getId());
        }
        theatre.setName(dto.getName());
        theatre.setAddress(dto.getAddress());
        theatre.setPhoneNumber(dto.getPhoneNumber());
        theatre.setEmail(dto.getEmail());
        theatre.setDescription(dto.getDescription());
        theatre.setTotalScreens(dto.getTotalScreens());
        theatre.setImageUrl(dto.getImageUrl());
        return theatre;
    }
}