package com.docuflow.api.entity;

import com.docuflow.api.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"user", "subscription"})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    // Amount in GHS kobo
    @Column(nullable = false)
    private int amountKobo;

    @Column(nullable = false, length = 3)
    private String currency = "GHS";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 255)
    private String paystackReference;

    @Column(length = 255)
    private String paystackTxId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> gatewayMetadata;

    @Column
    private Instant paidAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public static Payment create(User user, Subscription subscription,
                                 int amountKobo, String paystackReference) {
        var p = new Payment();
        p.user = user;
        p.subscription = subscription;
        p.amountKobo = amountKobo;
        p.status = PaymentStatus.PENDING;
        p.paystackReference = paystackReference;
        return p;
    }

    public void markCompleted(String txId, Map<String, Object> metadata) {
        this.status = PaymentStatus.COMPLETED;
        this.paystackTxId = txId;
        this.gatewayMetadata = metadata;
        this.paidAt = Instant.now();
    }

    public void markFailed(Map<String, Object> metadata) {
        this.status = PaymentStatus.FAILED;
        this.gatewayMetadata = metadata;
    }
}
