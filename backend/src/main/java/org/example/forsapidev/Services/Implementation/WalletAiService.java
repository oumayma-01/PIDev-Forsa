package org.example.forsapidev.Services.Implementation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Service
public class WalletAiService {

    private static final Logger log = LoggerFactory.getLogger(WalletAiService.class);

    @Value("${wallet.ai.url}")
    private String apiUrl;

    @Value("${wallet.ai.key}")
    private String apiKey;

    @Value("${wallet.ai.model}")
    private String model;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public String askAI(String systemPrompt, String userMessage) {
        try {
            String requestBody = mapper.writeValueAsString(Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user",   "content", userMessage)
                    ),
                    "temperature", 0.2,
                    "max_tokens", 500
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                log.error("Groq API error - status: {} body: {}",
                        response.statusCode(), response.body());
                throw new RuntimeException(
                        "Groq API a retourné le statut " + response.statusCode()
                                + " : " + response.body());
            }

            JsonNode root = mapper.readTree(response.body());
            String content = root.path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();

            log.info("Réponse IA reçue : {}", content);
            return content;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'appel à Groq : " + e.getMessage(), e);
        }
    }
}