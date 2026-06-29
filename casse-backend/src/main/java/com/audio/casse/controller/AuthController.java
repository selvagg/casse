package com.audio.casse.controller;

import com.audio.casse.config.OAuthVerifierResolver;
import com.audio.casse.dto.AuthResponse;
import com.audio.casse.dto.OAuthLoginRequest;
import com.audio.casse.models.AuthenticatedUser;
import com.audio.casse.models.OAuthProviderType;
import com.audio.casse.models.OAuthUserInfo;
import com.audio.casse.service.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuthVerifierResolver verifierResolver;
    private final JwtService jwtService;

    /**
     * POST /api/auth/google   (and, once implemented: /facebook, /instagram, /threads)
     * Body: {"token": "<provider token>"}
     * No persistence happens here - the verified claims go straight into the JWT.
     */
    @PostMapping("/{provider}")
    public ResponseEntity<AuthResponse> login(@PathVariable String provider,
                                              @Valid @RequestBody OAuthLoginRequest request) {

        OAuthProviderType type = OAuthProviderType.valueOf(provider.toUpperCase());
        OAuthUserInfo info = verifierResolver.resolve(type).verify(request.token());

        return ResponseEntity.ok(new AuthResponse(
                jwtService.generateAccessToken(info),
                jwtService.generateRefreshToken(info),
                "Bearer",
                jwtService.getAccessExpirySeconds()
        ));
    }

    @GetMapping("/whoami")
    public AuthenticatedUser whoAmI(@AuthenticationPrincipal(errorOnInvalidType = true) AuthenticatedUser user) {
        return user;
    }

}