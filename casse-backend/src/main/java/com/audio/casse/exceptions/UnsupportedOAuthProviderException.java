package com.audio.casse.exceptions;

import com.audio.casse.models.OAuthProviderType;

public class UnsupportedOAuthProviderException extends RuntimeException {
    public UnsupportedOAuthProviderException(OAuthProviderType type) {
        super("No verifier registered for provider: " + type);
    }
}
