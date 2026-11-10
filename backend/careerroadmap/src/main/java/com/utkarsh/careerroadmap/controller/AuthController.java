package com.utkarsh.careerroadmap.controller;

import com.utkarsh.careerroadmap.dto.RegisterRequest;
import com.utkarsh.careerroadmap.dto.RegisterResponse;
import com.utkarsh.careerroadmap.dto.LoginRequest;
import com.utkarsh.careerroadmap.dto.LoginResponse;

import com.utkarsh.careerroadmap.security.JwtService;
import com.utkarsh.careerroadmap.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @Valid @RequestBody RegisterRequest request) {

        if (authService.emailExists(request.getEmail())) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email is already registered");
        }

        RegisterResponse response = authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);

        // Generate HttpOnly Cookie containing the JWT token
        ResponseCookie cookie = jwtService.generateJwtCookie(response.getToken()); // cookie crreation

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("register/admin")
    public ResponseEntity<?> registerAdmin(
            @Valid @RequestBody RegisterRequest request) {

        if (authService.emailExists(request.getEmail())) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Email is already registered");
        }

        RegisterResponse response = authService.registerAdmin(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Clear the JWT cookie by expiring it immediately
        ResponseCookie cleanCookie = jwtService.getCleanJwtCookie();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanCookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            Authentication authentication) {

        return ResponseEntity.ok(
                Map.of(
                        "email", authentication.getName(),
                        "authorities", authentication.getAuthorities()
                                .stream()
                                .map(GrantedAuthority::getAuthority)
                                .toList()
                )
        );
    }
}
