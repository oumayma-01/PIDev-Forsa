package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.forsapidev.Repositories.AIScoreManagement.AIScoreRepository;
import org.example.forsapidev.Services.aiScoring.AIAgentClient;
import org.example.forsapidev.Services.aiScoring.AutoScoringService;
import org.example.forsapidev.entities.AIScoreManagement.AIScore;
import org.example.forsapidev.payload.response.AIScoreSummaryDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/api/ai-score")
@RequiredArgsConstructor
@Slf4j
public class AIScoreController {

    private final AIAgentClient aiAgentClient;
    private final AutoScoringService autoScoringService;
    private final AIScoreRepository aiScoreRepository;

    /**
     * Endpoint appelé par Angular avec les données du formulaire dans le body.
     * POST /api/ai-score/calculate-body/{clientId}
     * Body JSON : { monthlySalary, stegPaidOnTime, sondePaidOnTime, cinVerified }
     * Retourne un JSON camelCase compatible avec l'interface Angular AIScoreResponse.
     */
    @PostMapping("/calculate-body/{clientId}")
    public ResponseEntity<Map<String, Object>> calculateScoreBody(
            @PathVariable Long clientId,
            @RequestBody Map<String, Object> body) {

        log.info("Calcul score IA pour client {} — body={}", clientId, body);

        Double salary  = body.get("monthlySalary") != null
                ? Double.parseDouble(body.get("monthlySalary").toString()) : 0.0;
        Boolean steg   = (Boolean) body.getOrDefault("stegPaidOnTime", false);
        Boolean sonede = (Boolean) body.getOrDefault("sondePaidOnTime", false);
        Boolean cin    = (Boolean) body.getOrDefault("cinVerified", false);

        // Appel Python → retourne snake_case
        Map<String, Object> pythonResult = aiAgentClient.calculateScore(
                clientId, salary, steg, sonede, cin);

        // Conversion snake_case → camelCase pour Angular
        Map<String, Object> response = toCamelCase(pythonResult);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint legacy avec @RequestParam (utile pour tester sur Swagger).
     * POST /api/ai-score/calculate/{clientId}?monthlySalary=1500&stegPaidOnTime=true...
     */
    @PostMapping("/calculate/{clientId}")
    public ResponseEntity<Map<String, Object>> calculateScore(
            @PathVariable Long clientId,
            @RequestParam(defaultValue = "0") Double monthlySalary,
            @RequestParam(defaultValue = "false") Boolean stegPaidOnTime,
            @RequestParam(defaultValue = "false") Boolean sondePaidOnTime,
            @RequestParam(defaultValue = "false") Boolean cinVerified) {

        log.info("Calcul score IA pour client {} — salary={}", clientId, monthlySalary);
        Map<String, Object> pythonResult = aiAgentClient.calculateScore(
                clientId, monthlySalary, stegPaidOnTime, sondePaidOnTime, cinVerified);

        return ResponseEntity.ok(toCamelCase(pythonResult));
    }

    /**
     * Vérifie un document (CIN, STEG, SONEDE, SALARY) via le service Python.
     * POST /api/ai-score/verify-document
     * Multipart : file=<image>, document_type=CIN|STEG|SONEDE|SALARY
     */
    @PostMapping(value = "/verify-document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> verifyDocument(
            @RequestParam("document_type") String documentType,
            @RequestParam("file") MultipartFile file) {

        log.info("verify-document type={} file={}", documentType, file.getOriginalFilename());
        Map<String, Object> result = aiAgentClient.verifyDocument(documentType, file);
        return ResponseEntity.ok(result);
    }

    /**
     * Health check — vérifie que le controller est actif.
     * GET /api/ai-score/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok", "service", "AI Score Controller"));
    }

    /**
     * Retourne le dernier score IA d'un client.
     * GET /api/ai-score/current/{clientId}
     */
    @GetMapping("/current/{clientId}")
    public ResponseEntity<Map<String, Object>> getCurrentScore(@PathVariable Long clientId) {
        try {
            Optional<AIScore> opt = aiScoreRepository.findByClientId(clientId);
            if (opt.isPresent()) {
                return ResponseEntity.ok(toScoreDto(opt.get()));
            }
            AIScore created = autoScoringService.calculateAndSave(clientId);
            return ResponseEntity.ok(toScoreDto(created));
        } catch (Exception e) {
            log.error("getCurrentScore failed clientId=" + clientId, e);
            try {
                AIScore created = autoScoringService.calculateAndSave(clientId);
                return ResponseEntity.ok(toScoreDto(created));
            } catch (Exception e2) {
                log.error("getCurrentScore retry failed clientId=" + clientId, e2);
                return ResponseEntity.ok(minimalScoreDto(clientId));
            }
        }
    }

    /**
     * Recalcule et sauvegarde le score IA d'un client.
     * POST /api/ai-score/recalculate/{clientId}
     */
    @PostMapping("/recalculate/{clientId}")
    public ResponseEntity<Map<String, Object>> recalculateScore(@PathVariable Long clientId) {
        AIScore saved = autoScoringService.calculateAndSave(clientId);
        return ResponseEntity.ok(toScoreDto(saved));
    }

    /**
     * Active un booster STEG ou SONEDE, puis recalcule le score.
     * POST /api/ai-score/booster/{clientId}?type=STEG|SONEDE
     */
    @PostMapping("/booster/{clientId}")
    public ResponseEntity<Map<String, Object>> activateBooster(
            @PathVariable Long clientId,
            @RequestParam String type) {
        autoScoringService.activateBooster(clientId, type);
        AIScore saved = autoScoringService.calculateAndSave(clientId);
        return ResponseEntity.ok(toScoreDto(saved));
    }

    /**
     * POST /api/ai-score/boosters/{clientId}/activate?type=STEG|SONEDE
     */
    @PostMapping("/boosters/{clientId}/activate")
    public ResponseEntity<Map<String, Object>> activateBoosterWithRecalc(
            @PathVariable Long clientId,
            @RequestParam String type) {
        autoScoringService.activateBooster(clientId, type);
        AIScore saved = autoScoringService.calculateAndSave(clientId);
        return ResponseEntity.ok(toScoreDto(saved));
    }

    /**
     * Liste admin avec nom + email client.
     * GET /api/ai-score/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<AIScoreSummaryDto>> getAllScores() {
        return ResponseEntity.ok(autoScoringService.getAllScoreSummaries());
    }

    /**
     * GET /api/ai-score/summaries — alias de /all (compat).
     */
    @GetMapping("/summaries")
    public ResponseEntity<List<AIScoreSummaryDto>> getAllSummaries() {
        return ResponseEntity.ok(autoScoringService.getAllScoreSummaries());
    }

    private Map<String, Object> toScoreDto(AIScore s) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("clientId", s.getClientId());
        int sc = s.getCurrentScore() != null ? s.getCurrentScore() : 0;
        dto.put("score", sc);
        dto.put("currentScore", sc);
        dto.put("scoreLevel", s.getScoreLevel() != null ? s.getScoreLevel().name() : "VERY_LOW");
        dto.put("creditThreshold", s.getCreditThreshold() != null ? s.getCreditThreshold().doubleValue() : 0.0);
        dto.put("availableThreshold", s.getAvailableThreshold() != null ? s.getAvailableThreshold().doubleValue() : 0.0);
        dto.put("hasActiveCredit", Boolean.TRUE.equals(s.getHasActiveCredit()));
        dto.put("lastCalculatedAt", s.getLastCalculatedAt() != null ? s.getLastCalculatedAt().toString() : null);
        dto.put("aiExplanation", s.getAiExplanation());
        boolean stegActive = s.getStegBoosterExpiry() != null && s.getStegBoosterExpiry().isAfter(LocalDateTime.now());
        boolean sonedeActive = s.getSonedeBoosterExpiry() != null && s.getSonedeBoosterExpiry().isAfter(LocalDateTime.now());
        dto.put("stegBoosterActive", stegActive);
        dto.put("stegBoosterExpiry", s.getStegBoosterExpiry() != null ? s.getStegBoosterExpiry().toString() : null);
        dto.put("sonedeBoosterActive", sonedeActive);
        dto.put("sonedeBoosterExpiry", s.getSonedeBoosterExpiry() != null ? s.getSonedeBoosterExpiry().toString() : null);
        return dto;
    }

    private Map<String, Object> minimalScoreDto(long clientId) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("clientId", clientId);
        dto.put("score", 0);
        dto.put("currentScore", 0);
        dto.put("scoreLevel", "VERY_LOW");
        dto.put("creditThreshold", 0.0);
        dto.put("availableThreshold", 0.0);
        dto.put("hasActiveCredit", false);
        dto.put("lastCalculatedAt", null);
        dto.put("aiExplanation", null);
        dto.put("stegBoosterActive", false);
        dto.put("stegBoosterExpiry", null);
        dto.put("sonedeBoosterActive", false);
        dto.put("sonedeBoosterExpiry", null);
        return dto;
    }

    /**
     * Convertit la réponse Python (snake_case) en camelCase pour Angular.
     * Python renvoie : client_id, score_level, credit_threshold, salary_verified, features{f8_steg_on_time, ...}
     * Angular attend  : clientId, scoreLevel, creditThreshold, salaryVerified, features{...}
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> toCamelCase(Map<String, Object> python) {
        Map<String, Object> out = new HashMap<>();
        out.put("clientId",        python.getOrDefault("client_id", 0));
        out.put("score",           python.getOrDefault("score", 0));
        out.put("scoreLevel",      python.getOrDefault("score_level", "MEDIUM"));
        out.put("creditThreshold", python.getOrDefault("credit_threshold", 0.0));
        out.put("salaryVerified",  python.getOrDefault("salary_verified", 0.0));
        out.put("explanation",     python.getOrDefault("explanation", ""));

        // Features compatibles Angular (f1_salary, f2_income_stability, etc.)
        Object features = python.get("features");
        out.put("features", features != null ? features : Map.of());

        // Champs supplémentaires v3 (scoring intelligent)
        Object scoreDetails = python.get("score_details");
        out.put("scoreDetails", scoreDetails != null ? scoreDetails : Map.of());
        out.put("hasDbData", python.getOrDefault("has_db_data", false));

        return out;
    }
}
