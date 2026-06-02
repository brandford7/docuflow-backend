package com.docuflow.repository;

import com.docuflow.entity.Usage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UsageRepository extends JpaRepository<Usage, UUID> {

    Optional<Usage> findByUserIdAndDate(UUID userId, LocalDate date);
}
