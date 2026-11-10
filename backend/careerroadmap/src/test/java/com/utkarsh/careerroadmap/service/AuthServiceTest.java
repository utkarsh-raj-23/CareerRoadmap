
package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.LoginRequest;
import com.utkarsh.careerroadmap.dto.LoginResponse;
import com.utkarsh.careerroadmap.dto.RegisterRequest;
import com.utkarsh.careerroadmap.dto.RegisterResponse;

import com.utkarsh.careerroadmap.entity.User;
import com.utkarsh.careerroadmap.enums.Role;
import com.utkarsh.careerroadmap.repository.UserRepository;
import com.utkarsh.careerroadmap.security.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {

        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Test Student");
        registerRequest.setEmail("TEST@GMAIL.COM");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("TEST@GMAIL.COM");
        loginRequest.setPassword("password123");
    }

    // TEST 1: Check whether email already exists

    @Test
    void emailExistsShouldReturnTrue() {

        when(userRepository.existsByEmail("test@gmail.com"))
                .thenReturn(true);

        boolean result = authService.emailExists(" TEST@GMAIL.COM ");

        assertTrue(result);

        verify(userRepository).existsByEmail("test@gmail.com");
    }

    // TEST 2: Check whether email does not exist

    @Test
    void emailExistsShouldReturnFalse() {

        when(userRepository.existsByEmail("test@gmail.com"))
                .thenReturn(false);

        boolean result = authService.emailExists("TEST@GMAIL.COM");

        assertFalse(result);
    }

    // TEST 3: Register a new user successfully

    @Test
    void registerShouldSaveUserSuccessfully() {

        when(passwordEncoder.encode("password123"))
                .thenReturn("hashedPassword");

        RegisterResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("Registration successful", response.getMessage());
        assertEquals("test@gmail.com", response.getEmail());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(captor.capture());

        User savedUser = captor.getValue();

        assertEquals("Test Student", savedUser.getFullName());
        assertEquals("test@gmail.com", savedUser.getEmail());
        assertEquals("hashedPassword", savedUser.getPasswordHash());
        assertEquals(Role.STUDENT, savedUser.getRole());
        assertTrue(savedUser.isEnabled());

        verify(passwordEncoder).encode("password123");
    }

    // TEST 4: Login successfully with correct credentials

    @Test
    void loginShouldReturnTokenForValidCredentials() {

        User user = new User();
        user.setFullName("Test Student");
        user.setEmail("test@gmail.com");
        user.setPasswordHash("hashedPassword");
        user.setRole(Role.STUDENT);
        user.setEnabled(true);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password123", "hashedPassword"))
                .thenReturn(true);

        when(jwtService.generateToken("test@gmail.com"))
                .thenReturn("sample-jwt-token");

        LoginResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("Login successful", response.getMessage());
        assertEquals("sample-jwt-token", response.getToken());
        assertEquals("test@gmail.com", response.getEmail());
        assertEquals("Test Student", response.getFullName());

        verify(jwtService).generateToken("test@gmail.com");
    }

    // TEST 5: Login with an incorrect password

    @Test
    void loginShouldFailForIncorrectPassword() {

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPasswordHash("hashedPassword");
        user.setEnabled(true);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password123", "hashedPassword"))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );

        verify(jwtService, never()).generateToken(anyString());
    }

    // TEST 6: Login with an unregistered email

    @Test
    void loginShouldFailWhenUserDoesNotExist() {

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                exception.getStatusCode()
        );
    }

    // TEST 7: Login with a disabled account

    @Test
    void loginShouldFailWhenAccountIsDisabled() {

        User user = new User();
        user.setEmail("test@gmail.com");
        user.setPasswordHash("hashedPassword");
        user.setEnabled(false);

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches("password123", "hashedPassword"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authService.login(loginRequest)
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        verify(jwtService, never()).generateToken(anyString());
    }
}
