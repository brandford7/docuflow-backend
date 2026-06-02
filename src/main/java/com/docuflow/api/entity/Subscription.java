package com.docuflow.api.entity;

import com.docuflow.api.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"user", "plan"})
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    @Column(length = 100)
    private String paystackSubCode;

    @Column(length = 100)
    private String paystackEmailToken;

    @Column(nullable = false)
    private Instant currentPeriodStart;

    @Column(nullable = false)
    private Instant currentPeriodEnd;

    @Column
    private Instant cancelledAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public static Subscription create(User user, Plan plan,
                                      Instant periodStart, Instant periodEnd) {
        var s = new Subscription();
        s.user = user;
        s.plan = plan;
        s.status = SubscriptionStatus.TRIALING;
        s.currentPeriodStart = periodStart;
        s.currentPeriodEnd = periodEnd;
        return s;
    }

    public void activate(String subCode) {
        this.status = SubscriptionStatus.ACTIVE;
        this.paystackSubCode = subCode;
    }

    public void markPastDue() {
        this.status = SubscriptionStatus.PAST_DUE;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
        this.cancelledAt = Instant.now();
    }

    public void renewPeriod(Instant start, Instant end) {
        this.status = SubscriptionStatus.ACTIVE;
        this.currentPeriodStart = start;
        this.currentPeriodEnd = end;
    }

    public boolean isActive() {
        return status == SubscriptionStatus.ACTIVE
            || status == SubscriptionStatus.TRIALING;
    }
}
