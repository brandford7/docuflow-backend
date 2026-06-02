package com.docuflow.api.repository;

import com.docuflow.api.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    @Query("""
        SELECT s FROM Subscription s
        WHERE s.user.id = :userId
          AND s.status IN ('ACTIVE', 'TRIALING')
        ORDER BY s.createdAt DESC
        LIMIT 1
        """)
    Optional<Subscription> findActiveByUserId(@Param("userId") UUID userId);

    Optional<Subscription> findByPaystackSubCode(String paystackSubCode);
}
