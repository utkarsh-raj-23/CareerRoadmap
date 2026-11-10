package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.RegisterRequest;
import com.utkarsh.careerroadmap.dto.RegisterResponse;
import com.utkarsh.careerroadmap.dto.LoginRequest;
import com.utkarsh.careerroadmap.dto.LoginResponse;

import com.utkarsh.careerroadmap.entity.User;
import com.utkarsh.careerroadmap.enums.Role;
import com.utkarsh.careerroadmap.repository.UserRepository;
import com.utkarsh.careerroadmap.security.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Check whether email already exists
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(
                email.trim().toLowerCase()
        );
    }

    // Register a new user
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        User user = new User();

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        user.setRole(Role.STUDENT);
        user.setEnabled(true);

        userRepository.save(user);

        return new RegisterResponse(
                "Registration successful",
                user.getEmail()
        );
    }
    // Register a new admin
    @Transactional
    public RegisterResponse registerAdmin(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        User user = new User();

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        user.setRole(Role.ADMIN);
        user.setEnabled(true);

        userRepository.save(user);

        return new RegisterResponse(
                "Registration successful",
                user.getEmail()
        );
    }

    // Login existing user
    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())) {

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        if (!user.isEnabled()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account is disabled"
            );
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(
                "Login successful",
                token,
                user.getEmail(),
                user.getFullName()
        );
    }
}