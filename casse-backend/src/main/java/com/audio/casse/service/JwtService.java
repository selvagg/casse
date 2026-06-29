package com.audio.casse.service;

import com.audio.casse.models.OAuthUserInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey; // 256-bit+ random string from env var / secrets manager - never committed

    @Value("${jwt.access-token-expiry-seconds:3600}")
    private long accessExpiry;

    @Value("${jwt.refresh-token-expiry-seconds:604800}")
    private long refreshExpiry; // default lowered to 7 days vs. the DB-backed design's 30 -
    // there is no way to revoke a leaked refresh token here, so
    // expiry length is your only real lever. Tune deliberately.

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(OAuthUserInfo info) {
        return build(info, accessExpiry, "access");
    }

    public String generateRefreshToken(OAuthUserInfo info) {
        return build(info, refreshExpiry, "refresh");
    }

    private String build(OAuthUserInfo info, long expirySeconds, String type) {
        // Stable per-provider-account subject. No DB-generated ID exists, so this composite
        // string IS the user's identity. Same Google account -> same subject, every time.
        String subject = info.provider().name().toLowerCase() + ":" + info.providerUserId();

        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claim("provider", info.provider().name())
                .claim("email", info.email())
                .claim("emailVerified", info.emailVerified())
                .claim("name", info.name())
                .claim("picture", info.pictureUrl())
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expirySeconds)))
                .signWith(signingKey())
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
    }

    public long getAccessExpirySeconds() {
        return accessExpiry;
    }
}
