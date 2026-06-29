package com.audio.casse.models;

public record OAuthUserInfo(
        OAuthProviderType provider,
        String providerUserId,
        String email,
        boolean emailVerified,   // carried into the JWT as metadata only - has no behavioral effect server-side
        String name,
        String pictureUrl
) {}
