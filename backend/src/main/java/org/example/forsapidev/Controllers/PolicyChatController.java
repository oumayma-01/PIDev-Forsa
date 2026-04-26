package org.example.forsapidev.Controllers;

import org.example.forsapidev.DTO.ChatRequestDTO;
import org.example.forsapidev.DTO.ChatResponseDTO;
import org.example.forsapidev.Services.Implementation.PolicyChatService;
import org.example.forsapidev.entities.InsuranceManagement.ChatMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/insurance-chat")
@CrossOrigin(origins = "*")
public class PolicyChatController {

    private final PolicyChatService policyChatService;

    public PolicyChatController(PolicyChatService policyChatService) {
        this.policyChatService = policyChatService;
    }

    @PostMapping("/send")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<ChatResponseDTO> sendMessage(@RequestBody ChatRequestDTO request, Authentication auth) {
        try {
            ChatResponseDTO response = policyChatService.sendMessage(request.getPolicyId(), request.getMessage(), auth);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ChatResponseDTO.builder()
                    .reply("Error: " + e.getMessage())
                    .build());
        }
    }

    @GetMapping("/history/{policyId}")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<ChatMessage>> getHistory(@PathVariable Long policyId, Authentication auth) {
        try {
            List<ChatMessage> history = policyChatService.getHistory(policyId, auth);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
