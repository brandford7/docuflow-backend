package com.docuflow.api.entity;

import com.docuflow.api.enums.OperationFileRole;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "operation_files")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OperationFile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operation_id", nullable = false)
    private Operation operation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private OperationFileRole role;

    @Column
    private Integer sortOrder;

    public static OperationFile input(Operation op, File file, int sortOrder) {
        var of = new OperationFile();
        of.operation = op;
        of.file = file;
        of.role = OperationFileRole.INPUT;
        of.sortOrder = sortOrder;
        return of;
    }

    public static OperationFile output(Operation op, File file) {
        var of = new OperationFile();
        of.operation = op;
        of.file = file;
        of.role = OperationFileRole.OUTPUT;
        return of;
    }
}
