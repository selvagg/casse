package com.audio.casse.exceptions;

public class InvalidOAuthTokenException extends RuntimeException {
    public InvalidOAuthTokenException(String message) { super(message); }
    public InvalidOAuthTokenException(String message, Throwable cause) { super(message, cause); }
}
