package com.docuflow.api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.docuflow.api.dto.requests.LoginRequest;
import com.docuflow.api.dto.requests.RegisterRequest;
import com.docuflow.api.dto.response.AuthResponse;
import com.docuflow.api.entity.User;
import com.docuflow.api.repository.UserRepository;
import com.docuflow.api.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService; // 1. Inject the new service

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email is already in use");
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        User newUser = User.createUser(request.email(), request.username(), hashedPassword);
        User savedUser = userRepository.save(newUser);

        String accessToken = jwtService.generateToken(savedUser);
        // 2. Generate and save a database-backed token
        String refreshToken = refreshTokenService.createRefreshToken(savedUser); 
        
        return MapToAuthResponse(accessToken, refreshToken, savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) { // Removed readOnly=true because we update tokens
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String accessToken = jwtService.generateToken(user);
        // 3. Update the refresh token in DB upon every login
        String refreshToken = refreshTokenService.createRefreshToken(user); 

        return MapToAuthResponse(accessToken, refreshToken, user);
    }

    private AuthResponse MapToAuthResponse(String accessToken, String refreshToken, User user) {
        return new AuthResponse(
            accessToken,
            refreshToken,
            user.getId(),
            user.getEmail(),
            user.getUsername()
        );
    }
}