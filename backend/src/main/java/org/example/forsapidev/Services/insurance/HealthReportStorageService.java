package org.example.forsapidev.Services.insurance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class HealthReportStorageService {

    private static final Logger logger = LoggerFactory.getLogger(HealthReportStorageService.class);

    private final Path uploadDir;

    public HealthReportStorageService(
            @Value("${app.health-reports.upload-dir:uploads/health-reports}") String uploadDirStr) {
        this.uploadDir = Paths.get(uploadDirStr).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(uploadDir);
            logger.info("Health reports storage directory: {}", uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create upload directory: " + uploadDir, e);
        }
    }

    public String storeHealthReport(MultipartFile file, Long userId, Long creditId) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Health report file is empty");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String storedFilename = "health_report_" + userId + "_" + creditId
                + "_" + System.currentTimeMillis()
                + "_" + UUID.randomUUID().toString().substring(0, 8)
                + extension;

        try {
            Path target = uploadDir.resolve(storedFilename).normalize();
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            logger.info("Health report stored: {}", storedFilename);
            return storedFilename;
        } catch (IOException e) {
            throw new RuntimeException("Cannot store health report", e);
        }
    }

    public Path getFilePath(String filename) {
        return uploadDir.resolve(filename).normalize();
    }

    public void deleteHealthReport(String filename) {
        if (filename == null || filename.isBlank()) return;
        try {
            Path path = uploadDir.resolve(filename).normalize();
            Files.deleteIfExists(path);
            logger.info("Health report deleted: {}", filename);
        } catch (IOException e) {
            logger.warn("Cannot delete health report: {}", filename, e);
        }
    }
}

