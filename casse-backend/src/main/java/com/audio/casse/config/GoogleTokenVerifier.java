package com.audio.casse.config;

import com.audio.casse.exceptions.InvalidOAuthTokenException;
import com.audio.casse.models.OAuthProviderType;
import com.audio.casse.models.OAuthUserInfo;
import com.audio.casse.service.OAuthTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Component
public class GoogleTokenVerifier implements OAuthTokenVerifier {

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    void init() {
        verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(googleClientId)) // must equal the iOS app's OAuth client ID
                .build();
    }

    @Override
    public OAuthProviderType getProviderType() {
        return OAuthProviderType.GOOGLE;
    }

    @Override
    public OAuthUserInfo verify(String idTokenString) {
        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new InvalidOAuthTokenException("Invalid Google ID token");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            Boolean emailVerified = payload.getEmailVerified();
            return new OAuthUserInfo(
                    OAuthProviderType.GOOGLE,
                    payload.getSubject(),
                    payload.getEmail(),
                    Boolean.TRUE.equals(emailVerified),
                    (String) payload.get("name"),
                    (String) payload.get("picture")
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new InvalidOAuthTokenException("Failed to verify Google token", e);
        }
    }
}