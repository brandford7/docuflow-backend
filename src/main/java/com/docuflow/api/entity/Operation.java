package com.docuflow.api.entity;

import com.docuflow.api.enums.OperationStatus;
import com.docuflow.api.enums.OperationType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "operations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"user", "operationFiles"})
public class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OperationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperationStatus status = OperationStatus.QUEUED;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> inputParams;

    @Column(length = 1000)
    private String errorMessage;

    @Column
    private Integer durationMs;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant completedAt;

    @OneToMany(mappedBy = "operation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OperationFile> operationFiles = new ArrayList<>();

    public static Operation create(User user, OperationType type,
                                   Map<String, Object> params) {
        var op = new Operation();
        op.user = user;
        op.type = type;
        op.inputParams = params;
        op.status = OperationStatus.QUEUED;
        return op;
    }

    public void start() {
        this.status = OperationStatus.PROCESSING;
    }

    public void complete(int durationMs) {
        this.status = OperationStatus.COMPLETED;
        this.durationMs = durationMs;
        this.completedAt = Instant.now();
    }

    public void fail(String errorMessage) {
        this.status = OperationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.completedAt = Instant.now();
    }
}
