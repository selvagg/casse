package com.audio.casse.config;

import com.audio.casse.exceptions.InvalidOAuthTokenException;
import com.audio.casse.models.OAuthProviderType;
import com.audio.casse.models.OAuthUserInfo;
import com.audio.casse.service.OAuthTokenVerifier;
import org.springframework.web.client.RestClient;

/**
 * STUB - leave un-annotated (no @Component) until Facebook OAuth credentials exist,
 * so it doesn't register in OAuthVerifierResolver prematurely.
 */
public class FacebookTokenVerifier implements OAuthTokenVerifier {

    private final RestClient restClient = RestClient.create("https://graph.facebook.com");

    @Override
    public OAuthProviderType getProviderType() {
        return OAuthProviderType.FACEBOOK;
    }

    @Override
    public OAuthUserInfo verify(String accessToken) {
        FacebookMeResponse me = restClient.get()
                .uri("/me?fields=id,name,email,picture&access_token={token}", accessToken)
                .retrieve()
                .body(FacebookMeResponse.class);

        if (me == null) {
            throw new InvalidOAuthTokenException("Invalid Facebook access token");
        }
        boolean emailVerified = me.email() != null; // Graph API only returns email if confirmed
        return new OAuthUserInfo(OAuthProviderType.FACEBOOK, me.id(), me.email(), emailVerified, me.name(), me.picture());
    }

    record FacebookMeResponse(String id, String name, String email, String picture) {
    }
}
