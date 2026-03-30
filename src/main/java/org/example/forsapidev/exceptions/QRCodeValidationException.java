package org.example.forsapidev.exceptions;

public class QRCodeValidationException extends RuntimeException {
    public QRCodeValidationException(String message) {
        super(message);
    }
}