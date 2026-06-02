package com.docuflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "plans")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    // Price in GHS kobo (1 GHS = 100 kobo). 0 = free.
    @Column(nullable = false)
    private int priceMonthlyKobo;

    @Column(nullable = false)
    private int maxFileSizeMb;

    // -1 means unlimited
    @Column(nullable = false)
    private int maxOpsPerDay;

    @Column(nullable = false)
    private int storageLimitMb;

    @Column(nullable = false)
    private int fileExpiryDays;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public boolean isFree() {
        return priceMonthlyKobo == 0;
    }

    public boolean hasUnlimitedOps() {
        return maxOpsPerDay == -1;
    }
}
