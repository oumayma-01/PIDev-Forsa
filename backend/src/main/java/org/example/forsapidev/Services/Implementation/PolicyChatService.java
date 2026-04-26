package org.example.forsapidev.Services.Implementation;

import org.example.forsapidev.DTO.ChatResponseDTO;
import org.example.forsapidev.Repositories.ChatMessageRepository;
import org.example.forsapidev.Repositories.InsurancePolicyRepository;
import org.example.forsapidev.entities.InsuranceManagement.ChatMessage;
import org.example.forsapidev.entities.InsuranceManagement.InsuranceClaim;
import org.example.forsapidev.entities.InsuranceManagement.InsurancePolicy;
import org.example.forsapidev.entities.InsuranceManagement.PremiumPayment;
import org.example.forsapidev.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class PolicyChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final InsurancePolicyRepository insurancePolicyRepository;
    private final RestTemplate restTemplate;

    @Value("${openai.api-key:}")
    private String apiKey;

    @Value("${openai.base-url:https://api.groq.com/openai}")
    private String baseUrl;

    @Value("${openai.model:llama-3.3-70b-versatile}")
    private String model;

    public PolicyChatService(ChatMessageRepository chatMessageRepository, 
                             InsurancePolicyRepository insurancePolicyRepository) {
        this.chatMessageRepository = chatMessageRepository;
        this.insurancePolicyRepository = insurancePolicyRepository;
        this.restTemplate = new RestTemplate();
    }

    public ChatResponseDTO sendMessage(Long policyId, String userMessage, Authentication auth) throws Exception {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();

        System.out.println("Chat request for policy: " + policyId + " by user: " + userId);

        // 1. Verify policy ownership
        InsurancePolicy policy = insurancePolicyRepository.findById(policyId)
                .orElseThrow(() -> new Exception("Policy not found"));
        
        if (policy.getUser() == null || !policy.getUser().getId().equals(userId)) {
            throw new Exception("Unauthorized: You do not own this policy");
        }

        // 2. Build context
        String systemPrompt = buildSystemPrompt(policy);

        // 3. Get conversation history (last 8 messages to save tokens/context)
        List<ChatMessage> history = chatMessageRepository.findTop10ByPolicyIdOrderByTimestampDesc(policyId);
        Collections.reverse(history); 

        // 4. Prepare API request (OpenAI/Groq format)
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        List<Map<String, String>> messages = new ArrayList<>();
        
        // System message
        messages.add(Map.of("role", "system", "content", systemPrompt));

        // History messages
        for (ChatMessage msg : history) {
            messages.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
        }
        
        // Current user message
        messages.add(Map.of("role", "user", "content", userMessage));
        
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        // 5. Call API
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // Safety check for API Key - check properties then environment
        String effectiveApiKey = apiKey;
        if (effectiveApiKey == null || effectiveApiKey.isEmpty() || effectiveApiKey.contains("your_key_here")) {
            effectiveApiKey = System.getenv("GROQ_API_KEY");
        }

        if (effectiveApiKey == null || effectiveApiKey.isEmpty()) {
            return ChatResponseDTO.builder()
                    .reply("API Error: GROQ_API_KEY is not set. Please add it to your .env file or environment.")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
        
        headers.set("Authorization", "Bearer " + effectiveApiKey.trim());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String apiUrl = baseUrl.endsWith("/") ? baseUrl + "v1/chat/completions" : baseUrl + "/v1/chat/completions";
            System.out.println("Calling API: " + apiUrl);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choice = choices.get(0);
                    Map<String, Object> message = (Map<String, Object>) choice.get("message");
                    String assistantReply = (String) message.get("content");

                    // 6. Save messages to DB
                    saveMessage(policyId, userId, "user", userMessage);
                    saveMessage(policyId, userId, "assistant", assistantReply);

                    return ChatResponseDTO.builder()
                            .reply(assistantReply)
                            .timestamp(LocalDateTime.now())
                            .build();
                }
            }
            throw new Exception("Empty or invalid response from AI service");
            
        } catch (Exception e) {
            System.err.println("AI Service Error: " + e.getMessage());
            e.printStackTrace();
            return ChatResponseDTO.builder()
                    .reply("I'm currently unable to connect to the AI service (" + e.getMessage() + "). Please ensure your API key is valid.")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private void saveMessage(Long policyId, Long userId, String role, String content) {
        ChatMessage msg = ChatMessage.builder()
                .policyId(policyId)
                .userId(userId)
                .role(role)
                .content(content)
                .timestamp(LocalDateTime.now())
                .build();
        chatMessageRepository.save(msg);
    }

    public List<ChatMessage> getHistory(Long policyId, Authentication auth) throws Exception {
        UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
        Long userId = userDetails.getId();

        InsurancePolicy policy = insurancePolicyRepository.findById(policyId)
                .orElseThrow(() -> new Exception("Policy not found"));
        
        if (policy.getUser() == null || !policy.getUser().getId().equals(userId)) {
            throw new Exception("Unauthorized");
        }

        return chatMessageRepository.findByPolicyIdOrderByTimestampAsc(policyId);
    }

    private String buildSystemPrompt(InsurancePolicy policy) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are the Forsa Policy Assistant. Be professional, empathetic, and accurate.\n");
        sb.append("You are helping a client with Policy #").append(policy.getPolicyNumber()).append(".\n\n");
        
        sb.append("DATA CONTEXT:\n");
        sb.append("- Product: ").append(policy.getInsuranceProduct().getProductName()).append("\n");
        sb.append("- Status: ").append(policy.getStatus()).append("\n");
        sb.append("- Coverage: ").append(policy.getCoverageLimit()).append(" TND\n");
        sb.append("- Next Payment: ").append(policy.getNextPremiumDueDate()).append("\n");
        sb.append("- Amount Due: ").append(policy.getPeriodicPaymentAmount()).append(" TND\n\n");

        sb.append("GUIDELINES:\n");
        sb.append("- Use 'TND' for currency.\n");
        sb.append("- If you don't have enough data to answer a specific detail, politely refer the client to their agent.\n");
        sb.append("- Keep responses helpful but concise.\n");

        return sb.toString();
    }
}
