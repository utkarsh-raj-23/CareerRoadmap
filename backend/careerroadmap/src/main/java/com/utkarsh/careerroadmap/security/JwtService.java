package com.utkarsh.careerroadmap.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;

@Service
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey secretKey;
    private final long expiration;
    private final String cookieName;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration,
            @Value("${jwt.cookie-name:career_jwt}") String cookieName,
            @Value("${jwt.cookie-secure:false}") boolean cookieSecure,
            @Value("${jwt.cookie-same-site:Lax}") String cookieSameSite) {

        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        this.expiration = expiration;
        this.cookieName = cookieName;
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }

    // Generate JWT token string
    public String generateToken(String email) {
        Date currentDate = new Date();
        Date expiryDate = new Date(currentDate.getTime() + expiration);

        return Jwts.builder()
                .subject(email)
                .issuedAt(currentDate)
                .expiration(expiryDate)
                .signWith(secretKey)
                .compact();
    }

    // Create an HttpOnly ResponseCookie carrying the JWT
    public ResponseCookie generateJwtCookie(String token) {
        return ResponseCookie.from(cookieName, token)
                .path("/")
                .maxAge(Duration.ofMillis(expiration))
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .build();
    }

    // Create a clean/expired cookie to logout
    public ResponseCookie getCleanJwtCookie() {
        return ResponseCookie.from(cookieName, "")
                .path("/")
                .maxAge(Duration.ZERO)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .build();
    }

    // Extract JWT token from Cookies
    public String getJwtFromCookies(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        if (cookie != null && cookie.getValue() != null && !cookie.getValue().isBlank()) {
            return cookie.getValue();
        }
        return null;
    }

    // Extract email (subject) from JWT
    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    // Validate token and check expiration
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            logger.debug("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
