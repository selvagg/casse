package com.audio.casse.models;

public enum OAuthProviderType {
    GOOGLE, FACEBOOK, INSTAGRAM, THREADS,
    DEV // synthetic - used only by DevAuthController, never has a real OAuthTokenVerifier
}
