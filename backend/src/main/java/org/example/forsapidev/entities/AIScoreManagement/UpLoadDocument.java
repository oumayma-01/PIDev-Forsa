package org.example.forsapidev.entities.AIScoreManagement;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "uploaded_document")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpLoadDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long aiScoreRequestId;

    @Column(nullable = false)
    private Long clientId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(nullable = false)
    private String filePath;  // Ex: uploads/cin_123.jpg

    @Column(columnDefinition = "TEXT")
    private String ocrData;  // JSON des données extraites

    @Column(nullable = false)
    private Boolean verified = false;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;
}