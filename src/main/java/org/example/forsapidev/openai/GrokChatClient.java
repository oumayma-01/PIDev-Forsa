package org.example.forsapidev.openai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GrokChatClient { // Renommé pour correspondre à ton Assistant

    private final openai props; // Garde tes props ou renomme-les dans ta config
    private final ObjectMapper objectMapper;
    private final HttpClient http;

    public GrokChatClient(openai props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(props.getTimeoutSeconds()))
                .build();
    }

    public String chat(String systemPrompt, String userPrompt) {
        // Validation des propriétés Grok
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            throw new IllegalStateException("Missing Grok (xAI) API key");
        }

        try {
            Map<String, Object> body = Map.of(
                    "model", props.getModel(), // Ex: "grok-2-latest"
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "temperature", 0.2
            );

            String json = objectMapper.writeValueAsString(body);

            // CHANGEMENT : Grok n'utilise pas "/v1/chat/completions" après l'URL de base
            // si celle-ci inclut déjà la version. Vérifie bien ta config.
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(props.getBaseUrl() + "/chat/completions"))
                    .timeout(Duration.ofSeconds(props.getTimeoutSeconds()))
                    .header("Authorization", "Bearer " + props.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new RuntimeException("Grok error " + resp.statusCode() + " : " + resp.body());
            }

            JsonNode root = objectMapper.readTree(resp.body());
            JsonNode content = root.at("/choices/0/message/content");
            return content.isMissingNode() ? "" : content.asText().trim();

        } catch (Exception e) {
            throw new RuntimeException("Grok call failed: " + e.getMessage(), e);
        }
    }
}