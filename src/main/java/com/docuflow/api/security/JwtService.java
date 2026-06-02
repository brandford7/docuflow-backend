package com.docuflow.api.security;

import com.docuflow.api.config.AppProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

/**
 * Handles JWT generation and validation.
 *
 * Two token types:
 *
 * Access token  — short-lived (24h). Sent on every API request in the
 *                 Authorization: Bearer header. Contains the user's ID
 *                 and email as claims.
 *
 * Refresh token — long-lived (7d). Sent only to POST /api/auth/refresh
 *                 to get a new access token. Has a "type":"refresh" claim
 *                 so we can tell the two apart and reject a refresh token
 *                 used as an access token (and vice versa).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final AppProperties appProperties;

    // ── Token generation ──────────────────────────────────────────────────────

    public String generateAccessToken(UserDetails user, UUID userId) {
        return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("userId", userId.toString())
            .claim("type", "access")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis()
                + appProperties.getJwt().getAccessTokenExpiryMs()))
            .signWith(signingKey())
            .compact();
    }

    public String generateRefreshToken(UserDetails user) {
        return Jwts.builder()
            .setSubject(user.getUsername())
            .claim("type", "refresh")
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis()
                + appProperties.getJwt().getRefreshTokenExpiryMs()))
            .signWith(signingKey())
            .compact();
    }

    // ── Token validation ──────────────────────────────────────────────────────

    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            String email = extractEmail(token);
            return email.equals(userDetails.getUsername()) && !isExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            return "refresh".equals(extractClaims(token).get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    // ── Claim extraction ──────────────────────────────────────────────────────

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public UUID extractUserId(String token) {
        String id = extractClaims(token).get("userId", String.class);
        return id != null ? UUID.fromString(id) : null;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private boolean isExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(
            appProperties.getJwt().getSecret().getBytes(StandardCharsets.UTF_8));
    }
}
