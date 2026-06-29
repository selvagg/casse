package com.audio.casse.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidOAuthTokenException.class)
    public ResponseEntity<Map<String, String>> handleInvalidToken(InvalidOAuthTokenException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "invalid_token", "message", ex.getMessage()));
    }

    @ExceptionHandler(UnsupportedOAuthProviderException.class)
    public ResponseEntity<Map<String, String>> handleUnsupportedProvider(UnsupportedOAuthProviderException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "unsupported_provider", "message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadEnum(IllegalArgumentException ex) {
        // thrown by OAuthProviderType.valueOf() on an unrecognized {provider} path segment
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "invalid_provider_path", "message", "Unrecognized provider in URL path"));
    }
}
