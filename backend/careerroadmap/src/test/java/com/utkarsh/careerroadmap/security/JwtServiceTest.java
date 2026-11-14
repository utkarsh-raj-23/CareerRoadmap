
package com.utkarsh.careerroadmap.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private String secret;

    @BeforeEach
    void setUp() {
        // A 32-byte key, Base64 encoded
        byte[] keyBytes =
                "01234567890123456789012345678901".getBytes();

        secret = Base64.getEncoder().encodeToString(keyBytes);

        jwtService = new JwtService(secret, 86400000L, "career_jwt", false, "Lax");
    }

    @Test
    void generateTokenShouldReturnValidToken() {
        String email = "student@example.com";

        String token = jwtService.generateToken(email);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractEmailShouldReturnCorrectEmail() {
        String email = "student@example.com";

        String token = jwtService.generateToken(email);

        String extractedEmail = jwtService.extractEmail(token);

        assertEquals(email, extractedEmail);
    }

    @Test
    void generatedTokenShouldContainEmailAsSubject() {
        String email = "student@example.com";

        String token = jwtService.generateToken(email);

        assertEquals(email, jwtService.extractEmail(token));
    }

    @Test
    void extractEmailShouldRejectInvalidToken() {
        String invalidToken = "this.is.not.a.valid.jwt";

        assertThrows(
                JwtException.class,
                () -> jwtService.extractEmail(invalidToken)
        );
    }

    @Test
    void extractEmailShouldRejectTokenSignedWithDifferentSecret() {
        String email = "student@example.com";

        byte[] differentKeyBytes =
                "98765432109876543210987654321098".getBytes();

        SecretKey differentKey =
                Keys.hmacShaKeyFor(differentKeyBytes);

        String token = Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(differentKey)
                .compact();

        assertThrows(
                JwtException.class,
                () -> jwtService.extractEmail(token)
        );
    }

    @Test
    void extractEmailShouldRejectExpiredToken() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        String expiredToken = Jwts.builder()
                .subject("student@example.com")
                .issuedAt(new Date(System.currentTimeMillis() - 120000))
                .expiration(new Date(System.currentTimeMillis() - 60000))
                .signWith(key)
                .compact();

        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.extractEmail(expiredToken)
        );
    }
}
