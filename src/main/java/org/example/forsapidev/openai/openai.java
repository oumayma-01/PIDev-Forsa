package org.example.forsapidev.openai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the AI client (Grok/xAI).
 * The @Component annotation was removed to avoid duplicate bean definitions
 * with @EnableConfigurationProperties.
 */
@ConfigurationProperties(prefix = "openai")
public class openai {
    private boolean enabled = true;
    private String apiKey;

    // Default URL for xAI (Grok)
    private String baseUrl = "https://api.x.ai/v1";

    // Default model for Grok
    private String model = "grok-2-latest";

    private int timeoutSeconds = 30;

    // Standard Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getApiKey() {
        return apiKey;
    }
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getModel() {
        return model;
    }
    public void setModel(String model) {
        this.model = model;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }
}