package org.albarakadigital.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.albarakadigital.entity.enums.FileType;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Enumerated(EnumType.STRING)
    private FileType fileType;

    private String storagePath;

    private LocalDateTime uploadedAt;

    @OneToOne
    @JoinColumn(name = "operation_id")
    private Operation operation;

}