package com.docuflow.api.entity;

import com.docuflow.api.enums.FileStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(exclude = {"user", "sourceFile"})
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String originalName;

    @Column(nullable = false, unique = true, length = 1000)
    private String r2Key;

    @Column(nullable = false, length = 100)
    private String r2Bucket;

    @Column(nullable = false, length = 100)
    private String mimeType;

    @Column(nullable = false)
    private long sizeBytes;

    @Column
    private Integer pageCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FileStatus status = FileStatus.UPLOADING;

    @Column(nullable = false)
    private boolean isOutput = false;

    // For output files — points back to the primary input file
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_file_id")
    private File sourceFile;

    @Column
    private Instant expiresAt;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column
    private Instant deletedAt;

    public static File createUpload(User user, String originalName, String r2Key,
                                    String r2Bucket, String mimeType, long sizeBytes) {
        var f = new File();
        f.user = user;
        f.originalName = originalName;
        f.r2Key = r2Key;
        f.r2Bucket = r2Bucket;
        f.mimeType = mimeType;
        f.sizeBytes = sizeBytes;
        f.status = FileStatus.UPLOADING;
        f.isOutput = false;
        return f;
    }

    public static File createOutput(User user, String originalName, String r2Key,
                                    String r2Bucket, String mimeType, long sizeBytes,
                                    File sourceFile) {
        var f = new File();
        f.user = user;
        f.originalName = originalName;
        f.r2Key = r2Key;
        f.r2Bucket = r2Bucket;
        f.mimeType = mimeType;
        f.sizeBytes = sizeBytes;
        f.status = FileStatus.READY;
        f.isOutput = true;
        f.sourceFile = sourceFile;
        return f;
    }

    public void markReady(Integer pageCount) {
        this.status = FileStatus.READY;
        this.pageCount = pageCount;
    }

    public void markProcessing() {
        this.status = FileStatus.PROCESSING;
    }

    public void setExpiry(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void softDelete() {
        this.status = FileStatus.DELETED;
        this.deletedAt = Instant.now();
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}
