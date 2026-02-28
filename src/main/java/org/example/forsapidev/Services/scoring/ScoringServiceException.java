package org.example.forsapidev.Services.scoring;

/**
 * Exception levée en cas d'erreur lors du scoring via l'IA
 */
public class ScoringServiceException extends RuntimeException {

    public ScoringServiceException(String message) {
        super(message);
    }

    public ScoringServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}

