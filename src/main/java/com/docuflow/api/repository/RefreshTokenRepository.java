package com.docuflow.api.repository;

import com.docuflow.api.entity.RefreshToken;
import com.docuflow.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user); // For "Logout from all devices"
}