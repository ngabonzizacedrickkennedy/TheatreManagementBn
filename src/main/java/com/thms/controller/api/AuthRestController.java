package com.thms.controller.api;

import com.thms.dto.ApiResponse;
import com.thms.dto.UserDTO;
import com.thms.dto.auth.LoginRequest;
import com.thms.dto.auth.LoginResponse;
import com.thms.dto.auth.SignupRequest;
import com.thms.exception.ResourceNotFoundException;
import com.thms.security.JwtTokenProvider;
import com.thms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthRestController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthRestController(AuthenticationManager authenticationManager, 
                              JwtTokenProvider tokenProvider, 
                              UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        LoginResponse loginResponse = new LoginResponse(jwt, userDetails.getUsername(), userDetails.getAuthorities().toString());

        return ResponseEntity.ok(ApiResponse.success(loginResponse, "User logged in successfully"));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserDTO>> registerUser(@Valid @RequestBody SignupRequest signupRequest) {
        // Check if username already exists
        if (userService.existsByUsername(signupRequest.getUsername())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Username is already taken!"));
        }

        // Check if email already exists
        if (userService.existsByEmail(signupRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Email is already in use!"));
        }

        // Create new user
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername(signupRequest.getUsername());
        userDTO.setEmail(signupRequest.getEmail());
        userDTO.setPassword(signupRequest.getPassword());
        userDTO.setFirstName(signupRequest.getFirstName());
        userDTO.setLastName(signupRequest.getLastName());
        userDTO.setPhoneNumber(signupRequest.getPhoneNumber());
        
        UserDTO createdUser = userService.registerUser(userDTO);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdUser, "User registered successfully"));
    }
} 