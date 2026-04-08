package org.example.forsapidev.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration pour le client HTTP du service de scoring IA
 */
@Configuration
public class ScoringClientConfig {

    @Value("${ai.scoring.connect-timeout-ms:5000}")
    private int connectTimeout;

    @Value("${ai.scoring.read-timeout-ms:10000}")
    private int readTimeout;

    @Bean(name = "scoringRestTemplate")
    public RestTemplate scoringRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        RestTemplate restTemplate = new RestTemplate(factory);
        return restTemplate;
    }
}

