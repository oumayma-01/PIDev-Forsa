package org.example.forsapidev.Controllers;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.example.forsapidev.openai.ComplaintAiAssistant;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@SecurityRequirement(name = "Bearer Authentication")
public class ChatbotController {

    private final ComplaintAiAssistant complaintAiAssistant;

    public ChatbotController(ComplaintAiAssistant complaintAiAssistant) {
        this.complaintAiAssistant = complaintAiAssistant;
    }

    @PostMapping("/ask")
    @PreAuthorize("hasAnyRole('CLIENT','ADMIN','AGENT')")
    public Map<String, String> chat(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "");

        String answer;
        try {
            answer = complaintAiAssistant.draftResponse(
                    "SUPPORT",
                    "General Question",
                    message
            );
        } catch (Exception e) {
            answer = "I am a virtual assistant. To create a claim, use the \"New Claim\" button "
                    + "and describe your issue. You can then track its status (OPEN, IN_PROGRESS, RESOLVED, CLOSED).";
        }

        return Map.of("answer", answer);
    }
}