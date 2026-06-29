package com.audio.casse.models;

/**
 * The full identity of the caller, as decoded straight off the JWT's claims.
 * This is the ONLY representation of "the current user" anywhere in this system -
 * there is no service to look it up from, so every field a controller needs must
 * already be on the token.
 */
public record AuthenticatedUser(String subject, String provider, String email, String name, String pictureUrl) {}
