package com.audio.casse.controller;

import com.audio.casse.dto.AuthResponse;
import com.audio.casse.models.OAuthProviderType;
import com.audio.casse.models.OAuthUserInfo;
import com.audio.casse.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Backend-only auth for local development - mints a JWT for any email you pass in,
 * with no verification and no record kept anywhere.
 *
 * Because this system has NO persistence layer at all, there is nothing here to audit
 * or notice afterward if this endpoint is ever reachable somewhere it shouldn't be -
 * unlike a DB-backed design, a stray prod hit leaves zero trace. The @Profile guard
 * below is the ONLY thing standing between "local testing tool" and "anyone can mint
 * a valid token for any email." Treat it accordingly.
 */
@RestController
@RequestMapping("/api/dev-auth")
@RequiredArgsConstructor
@Profile({"dev", "local"})
public class DevAuthController {

    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> devLogin(@RequestParam(defaultValue = "test@casse.com") String email) {
        OAuthUserInfo info = new OAuthUserInfo(OAuthProviderType.DEV, email, email, true, "Dev Test User", null);

        return ResponseEntity.ok(new AuthResponse(
                jwtService.generateAccessToken(info),
                jwtService.generateRefreshToken(info),
                "Bearer",
                jwtService.getAccessExpirySeconds()
        ));
    }
}
