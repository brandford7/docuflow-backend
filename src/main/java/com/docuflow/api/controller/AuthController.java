package com.docuflow.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.docuflow.api.dto.requests.LoginRequest;
import com.docuflow.api.dto.requests.RefreshTokenRequest;
import com.docuflow.api.dto.requests.RegisterRequest;
import com.docuflow.api.dto.response.AuthResponse;
import com.docuflow.api.entity.RefreshToken;
import com.docuflow.api.security.JwtService;
import com.docuflow.api.service.AuthService;
import com.docuflow.api.service.RefreshTokenService;

import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {

        return ResponseEntity.ok(authService.register(request));
    }
    
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
}
    
@PostMapping("/refresh")
public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
    return refreshTokenService.findByToken(request.refreshToken())
            .map(refreshTokenService::verifyExpiration)
            .map(RefreshToken::getUser)
            .map(user -> {
                String accessToken = jwtService.generateToken(user);
                return ResponseEntity.ok(new AuthResponse(
                    accessToken, 
                    request.refreshToken(), // Keep the same refresh token or rotate it
                    user.getId(), 
                    user.getEmail(), 
                    user.getUsername()));
            })
            .orElseThrow(() -> new RuntimeException("Refresh token is not in database!"));
}

}
