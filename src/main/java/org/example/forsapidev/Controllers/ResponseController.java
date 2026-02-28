package org.example.forsapidev.Controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.forsapidev.entities.ComplaintFeedbackManagement.Response;
import org.example.forsapidev.Services.Interfaces.IResponseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/responses")
@RequiredArgsConstructor
public class ResponseController {

    private final IResponseService responseService;

    @GetMapping("/retrieve-all-responses")
    public ResponseEntity<List<Response>> getResponses() {
        List<Response> responses = responseService.retrieveAllResponses();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/retrieve-response/{response-id}")
    public ResponseEntity<Response> retrieveResponse(
            @PathVariable("response-id") Long rId) {
        Response response = responseService.retrieveResponse(rId);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(response);
    }

    // ✅ AJOUT : @Valid pour activer la validation
    @PostMapping("/add-response")
    public ResponseEntity<Response> addResponse(
            @Valid @RequestBody Response r) {
        Response saved = responseService.addResponse(r);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @DeleteMapping("/remove-response/{response-id}")
    public ResponseEntity<Void> removeResponse(
            @PathVariable("response-id") Long rId) {
        responseService.removeResponse(rId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    // ✅ AJOUT : @Valid pour activer la validation
    @PutMapping("/modify-response")
    public ResponseEntity<Response> modifyResponse(
            @Valid @RequestBody Response r) {
        Response updated = responseService.modifyResponse(r);
        return ResponseEntity.ok(updated);
    }
    @GetMapping("/report/summary")
    public ResponseEntity<Map<String, Object>> responseSummaryReport() {
        return ResponseEntity.ok(responseService.getResponseSummaryReport());
    }


    // ================================================
    // ✅ GESTION DES ERREURS DE VALIDATION
    // ================================================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {

        Map<String, Object> errors = new LinkedHashMap<>();
        errors.put("timestamp", new Date());
        errors.put("status", HttpStatus.BAD_REQUEST.value());
        errors.put("error", "Erreur de validation");

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        errors.put("messages", fieldErrors);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }
    @PutMapping("/improve-response-ai/{response-id}")
    public ResponseEntity<Response> improveResponseAI(@PathVariable("response-id") Long rId) {
        Response updated = responseService.improveResponseWithAI(rId);
        if (updated == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(updated);
    }

}
