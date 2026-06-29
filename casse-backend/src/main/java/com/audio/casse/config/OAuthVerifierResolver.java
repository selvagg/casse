package com.audio.casse.config;

import com.audio.casse.exceptions.UnsupportedOAuthProviderException;
import com.audio.casse.models.OAuthProviderType;
import com.audio.casse.service.OAuthTokenVerifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class OAuthVerifierResolver {

    private final Map<OAuthProviderType, OAuthTokenVerifier> verifiers;

    public OAuthVerifierResolver(List<OAuthTokenVerifier> verifierBeans) {
        this.verifiers = verifierBeans.stream()
                .collect(Collectors.toMap(OAuthTokenVerifier::getProviderType, Function.identity()));
    }

    public OAuthTokenVerifier resolve(OAuthProviderType type) {
        OAuthTokenVerifier v = verifiers.get(type);
        if (v == null) {
            throw new UnsupportedOAuthProviderException(type);
        }
        return v;
    }
}
