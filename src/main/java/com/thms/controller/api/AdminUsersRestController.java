package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.dto.UserDTO;
import com.thms.exception.ResourceNotFoundException;
import com.thms.model.User;
import com.thms.service.BookingService;
import com.thms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin User Management", description = "API endpoints for managing users (Admin only)")
public class AdminUsersRestController {

    private final UserService userService;
    private final BookingService bookingService;

    public AdminUsersRestController(UserService userService, BookingService bookingService) {
        this.userService = userService;
        this.bookingService = bookingService;
    }

    /**
     * Get all users with optional filtering and pagination
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieve all users with optional filtering")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "username") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        try {
            List<UserDTO> users = userService.getAllUsers();
            
            // Filter by search query if provided
            if (search != null && !search.trim().isEmpty()) {
                String searchLower = search.toLowerCase().trim();
                users = users.stream()
                    .filter(user -> 
                        user.getUsername().toLowerCase().contains(searchLower) ||
                        user.getEmail().toLowerCase().contains(searchLower) ||
                        (user.getFirstName() + " " + user.getLastName()).toLowerCase().contains(searchLower))
                    .toList();
            }
            
            // Filter by role if provided
            if (role != null && !role.trim().isEmpty()) {
                users = users.stream()
                    .filter(user -> user.getRole().name().equals(role))
                    .toList();
            }
            
            // Sort users
            users = users.stream()
                .sorted((a, b) -> {
                    int comparison = 0;
                    switch (sortBy) {
                        case "username":
                            comparison = a.getUsername().compareToIgnoreCase(b.getUsername());
                            break;
                        case "email":
                            comparison = a.getEmail().compareToIgnoreCase(b.getEmail());
                            break;
                        case "fullName":
                            String fullNameA = a.getFirstName() + " " + a.getLastName();
                            String fullNameB = b.getFirstName() + " " + b.getLastName();
                            comparison = fullNameA.compareToIgnoreCase(fullNameB);
                            break;
                        case "role":
                            comparison = a.getRole().name().compareToIgnoreCase(b.getRole().name());
                            break;
                        default:
                            comparison = a.getUsername().compareToIgnoreCase(b.getUsername());
                    }
                    return "desc".equalsIgnoreCase(sortOrder) ? -comparison : comparison;
                })
                .toList();
            
            return ResponseEntity.ok(ApiResponse.success(users));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    /**
     * Get a user by ID with their bookings
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieve user details with their booking history")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserById(@PathVariable Long id) {
        try {
            UserDTO user = userService.getUserById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            
            // Get user's bookings
            try {
                response.put("bookings", bookingService.getBookingsByUser(id));
            } catch (Exception e) {
                // If bookings fail, still return user data
                response.put("bookings", List.of());
            }
            
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound()
                    .build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve user: " + e.getMessage()));
        }
    }

    /**
     * Create a new user
     */
    @PostMapping
    @Operation(summary = "Create user", description = "Create a new user account")
    public ResponseEntity<ApiResponse<UserDTO>> createUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            // Validate required fields
            if (userDTO.getUsername() == null || userDTO.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Username is required"));
            }
            
            if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is required"));
            }
            
            if (userDTO.getPassword() == null || userDTO.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Password is required"));
            }
            
            // Check if username already exists
            if (userService.existsByUsername(userDTO.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Username is already taken"));
            }
            
            // Check if email already exists
            if (userService.existsByEmail(userDTO.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is already in use"));
            }
            
            // Set default role if not provided
            if (userDTO.getRole() == null) {
                userDTO.setRole(User.Role.ROLE_USER);
            }
            
            UserDTO createdUser = userService.createUser(userDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(createdUser, "User created successfully"));
                    
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to create user: " + e.getMessage()));
        }
    }

    /**
     * Update an existing user
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Update an existing user's information")
    public ResponseEntity<ApiResponse<UserDTO>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO) {
        
        try {
            // Check if user exists
            UserDTO existingUser = userService.getUserById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
            
            // Validate unique constraints (only if values are being changed)
            if (!existingUser.getUsername().equals(userDTO.getUsername()) && 
                userService.existsByUsername(userDTO.getUsername())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Username is already taken"));
            }
            
            if (!existingUser.getEmail().equals(userDTO.getEmail()) && 
                userService.existsByEmail(userDTO.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Email is already in use"));
            }
            
            // Update the user
            userDTO.setId(id);
            UserDTO updatedUser = userService.updateUser(id, userDTO)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
            
            return ResponseEntity.ok(ApiResponse.success(updatedUser, "User updated successfully"));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update user: " + e.getMessage()));
        }
    }

    /**
     * Update user role
     */
    @PutMapping("/{id}/role")
    @Operation(summary = "Update user role", description = "Update a user's role")
    public ResponseEntity<ApiResponse<UserDTO>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> roleRequest) {
        
        try {
            String roleString = roleRequest.get("role");
            if (roleString == null || roleString.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Role is required"));
            }
            
            User.Role role;
            try {
                role = User.Role.valueOf(roleString);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Invalid role: " + roleString));
            }
            
            UserDTO updatedUser = userService.updateUserRole(id, role)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
            
            return ResponseEntity.ok(ApiResponse.success(updatedUser, "User role updated successfully"));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to update user role: " + e.getMessage()));
        }
    }

    /**
     * Delete a user
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Delete a user account")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        try {
            // Check if user exists
            UserDTO user = userService.getUserById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
            
            // Prevent deletion of admin users (optional safety measure)
            if (user.getRole() == User.Role.ROLE_ADMIN) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Cannot delete admin users"));
            }
            
            userService.deleteUser(id);
            return ResponseEntity.ok(ApiResponse.success(null, "User deleted successfully"));
            
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to delete user: " + e.getMessage()));
        }
    }

    /**
     * Get user statistics
     */
    @GetMapping("/stats")
    @Operation(summary = "Get user statistics", description = "Get user statistics by role")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats() {
        try {
            List<UserDTO> allUsers = userService.getAllUsers();
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", allUsers.size());
            stats.put("adminUsers", allUsers.stream().filter(u -> u.getRole() == User.Role.ROLE_ADMIN).count());
            stats.put("managerUsers", allUsers.stream().filter(u -> u.getRole() == User.Role.ROLE_MANAGER).count());
            stats.put("regularUsers", allUsers.stream().filter(u -> u.getRole() == User.Role.ROLE_USER).count());
            
            return ResponseEntity.ok(ApiResponse.success(stats));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to retrieve user statistics: " + e.getMessage()));
        }
    }

    /**
     * Check if username exists
     */
    @GetMapping("/check-username")
    @Operation(summary = "Check username availability", description = "Check if a username is already taken")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkUsername(@RequestParam String username) {
        try {
            boolean exists = userService.existsByUsername(username);
            Map<String, Boolean> result = new HashMap<>();
            result.put("exists", exists);
            result.put("available", !exists);
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to check username: " + e.getMessage()));
        }
    }

    /**
     * Check if email exists
     */
    @GetMapping("/check-email")
    @Operation(summary = "Check email availability", description = "Check if an email is already in use")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkEmail(@RequestParam String email) {
        try {
            boolean exists = userService.existsByEmail(email);
            Map<String, Boolean> result = new HashMap<>();
            result.put("exists", exists);
            result.put("available", !exists);
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error("Failed to check email: " + e.getMessage()));
        }
    }
}