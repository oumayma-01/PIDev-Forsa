package org.example.forsapidev.Repositories.AIScoreManagement;

import org.example.forsapidev.entities.AIScoreManagement.UpLoadDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UploadedDocumentRepository extends JpaRepository<UpLoadDocument, Long> {
    List<UpLoadDocument> findByAiScoreRequestId(Long requestId);
    List<UpLoadDocument> findByClientId(Long clientId);
}