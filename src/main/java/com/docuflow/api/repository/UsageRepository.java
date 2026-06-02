package com.docuflow.api.repository;

import com.docuflow.api.entity.Usage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsageRepository extends JpaRepository<Usage, UUID> {

    Optional<Usage> findByUserIdAndDate(UUID userId, LocalDate date);
}
