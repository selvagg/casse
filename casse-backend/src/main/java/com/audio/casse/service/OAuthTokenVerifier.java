package com.audio.casse.service;

import com.audio.casse.exceptions.InvalidOAuthTokenException;
import com.audio.casse.models.OAuthProviderType;
import com.audio.casse.models.OAuthUserInfo;

public interface OAuthTokenVerifier {
    OAuthProviderType getProviderType();

    /**
     * @throws InvalidOAuthTokenException if the token is invalid/expired/forged
     */
    OAuthUserInfo verify(String token);
}
