package com.docuflow.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "usage")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Usage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int opsCount = 0;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public static Usage forToday(User user) {
        var u = new Usage();
        u.user = user;
        u.date = LocalDate.now();
        return u;
    }

    public void increment() {
        this.opsCount++;
    }
}
