package org.example.forsapidev;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ForsaPidevApplication {

    public static void main(String[] args) {
        SpringApplication.run(ForsaPidevApplication.class, args);
    }

    /**
     * Configure Jackson pour :
     * - Autoriser les zéros initiaux dans les nombres (ex: 0700000)
     * - Ignorer les propriétés inconnues dans le JSON entrant
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                .featuresToEnable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS)
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }
}
