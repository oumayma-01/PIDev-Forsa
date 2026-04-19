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
        System.out.println("Current Working Directory: " + System.getProperty("user.dir"));
        
        // Try to load from current directory first, then from 'backend/' if not found
        io.github.cdimascio.dotenv.Dotenv dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
                .directory("./")
                .ignoreIfMissing()
                .load();
        
        if (dotenv.get("STRIPE_SECRET_KEY") == null) {
            System.out.println(".env not found in ./, trying ./backend");
            dotenv = io.github.cdimascio.dotenv.Dotenv.configure()
                    .directory("./backend")
                    .ignoreIfMissing()
                    .load();
        }

        if (dotenv.get("STRIPE_SECRET_KEY") != null) {
            System.out.println("STRIPE_SECRET_KEY found! Setting system property.");
            dotenv.entries().forEach(entry -> {
                System.setProperty(entry.getKey(), entry.getValue());
            });
        } else {
            System.err.println("STRIPE_SECRET_KEY NOT FOUND in .env files!");
        }
        
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
