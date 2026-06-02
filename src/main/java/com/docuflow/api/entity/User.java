package com.docuflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"files", "operations"})
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(length = 150)
    private String fullName;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(length = 64)
    private String verificationToken;

    @Column(length = 64)
    private String resetToken;

    @Column
    private Instant resetTokenExpires;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @Column
    private Instant deletedAt;

    // ── UserDetails ───────────────────────────────────────────────────────────

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Simple role model — all users have ROLE_USER.
        // Admin operations are restricted by checking email in SecurityConfig.
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return email; }

    @Override
    public boolean isAccountNonLocked() { return deletedAt == null; }

    @Override
    public boolean isEnabled() { return deletedAt == null; }

    // ── Factory ───────────────────────────────────────────────────────────────

    public static User create(String email, String passwordHash, String fullName) {
        var u = new User();
        u.email = email.toLowerCase().strip();
        u.passwordHash = passwordHash;
        u.fullName = fullName;
        return u;
    }

    // ── Domain behaviour ──────────────────────────────────────────────────────

    public void verifyEmail() {
        this.emailVerified = true;
        this.verificationToken = null;
    }

    public void setVerificationToken(String token) {
        this.verificationToken = token;
    }

    public void setResetToken(String token, Instant expires) {
        this.resetToken = token;
        this.resetTokenExpires = expires;
    }

    public void clearResetToken() {
        this.resetToken = null;
        this.resetTokenExpires = null;
    }

    public void updatePassword(String newHash) {
        this.passwordHash = newHash;
        clearResetToken();
    }

    public void updateProfile(String fullName) {
        if (fullName != null && !fullName.isBlank()) this.fullName = fullName;
    }

    public void softDelete() {
        this.deletedAt = Instant.now();
    }

    public boolean isResetTokenValid() {
        return resetToken != null
            && resetTokenExpires != null
            && Instant.now().isBefore(resetTokenExpires);
    }
}
