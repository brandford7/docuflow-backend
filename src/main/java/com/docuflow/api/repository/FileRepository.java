package com.docuflow.api.repository;

import com.docuflow.api.entity.File;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<File, UUID> {

    @Query("""
        SELECT f FROM File f
        WHERE f.user.id = :userId
          AND f.deletedAt IS NULL
        ORDER BY f.createdAt DESC
        """)
    Page<File> findActiveByUserId(@Param("userId") UUID userId, Pageable pageable);

    Optional<File> findByIdAndUserIdAndDeletedAtIsNull(UUID id, UUID userId);

    @Query("SELECT COALESCE(SUM(f.sizeBytes), 0) FROM File f WHERE f.user.id = :userId AND f.deletedAt IS NULL")
    long sumStorageByUserId(@Param("userId") UUID userId);

    List<File> findByExpiresAtBeforeAndDeletedAtIsNull(Instant now);
}
