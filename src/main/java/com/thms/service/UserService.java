package com.thms.service;

import com.thms.dto.UserDTO;
import com.thms.model.User;
import com.thms.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public UserDTO registerUser(UserDTO userDTO) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setRole(User.Role.ROLE_USER); // Default role for new registrations

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Transactional
    public UserDTO createUser(UserDTO userDTO) {
        // Check if username or email already exists
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("Username is already taken!");
        }

        if (userRepository.existsByEmail(userDTO.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // Create new user (similar to registerUser but allows setting custom roles)
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setPhoneNumber(userDTO.getPhoneNumber());

        // Use the role from userDTO or default to ROLE_USER
        user.setRole(userDTO.getRole() != null ? userDTO.getRole() : User.Role.ROLE_USER);

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id).map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(this::convertToDTO);
    }

    @Transactional
    public Optional<UserDTO> updateUser(Long id, UserDTO userDTO) {
        return userRepository.findById(id).map(user -> {
            // Update username if provided and different
            if (userDTO.getUsername() != null && !userDTO.getUsername().trim().isEmpty() &&
                    !user.getUsername().equals(userDTO.getUsername())) {

                // Check if new username is already taken
                if (userRepository.existsByUsername(userDTO.getUsername())) {
                    throw new RuntimeException("Username is already taken!");
                }
                user.setUsername(userDTO.getUsername());
            }

            // Update email if provided and different
            if (userDTO.getEmail() != null && !userDTO.getEmail().trim().isEmpty() &&
                    !user.getEmail().equals(userDTO.getEmail())) {

                // Check if new email is already taken
                if (userRepository.existsByEmail(userDTO.getEmail())) {
                    throw new RuntimeException("Email is already in use!");
                }
                user.setEmail(userDTO.getEmail());
            }

            // Update other fields
            if (userDTO.getFirstName() != null && !userDTO.getFirstName().trim().isEmpty()) {
                user.setFirstName(userDTO.getFirstName());
            }

            if (userDTO.getLastName() != null && !userDTO.getLastName().trim().isEmpty()) {
                user.setLastName(userDTO.getLastName());
            }

            if (userDTO.getPhoneNumber() != null) {
                user.setPhoneNumber(userDTO.getPhoneNumber());
            }

            // Update role if provided
            if (userDTO.getRole() != null) {
                user.setRole(userDTO.getRole());
            }

            // Only update password if provided
            if (userDTO.getPassword() != null && !userDTO.getPassword().trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
            }

            return convertToDTO(userRepository.save(user));
        });
    }

    @Transactional
    public Optional<UserDTO> updateUserRole(Long id, User.Role role) {
        return userRepository.findById(id).map(user -> {
            user.setRole(role);
            return convertToDTO(userRepository.save(user));
        });
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setRole(user.getRole());
        // Don't set password in DTO for security reasons
        return dto;
    }
}