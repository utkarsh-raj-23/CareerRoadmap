
package com.utkarsh.careerroadmap.security;

import com.utkarsh.careerroadmap.entity.User;
import com.utkarsh.careerroadmap.repository.UserRepository;

import jakarta.servlet.FilterChain;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FilterChain filterChain;

    @Mock
    private User user;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtService, userRepository);

        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsMissing() throws Exception {
        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    void shouldContinueWhenAuthorizationHeaderIsNotBearer() throws Exception {
        request.addHeader("Authorization", "Basic abc123");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userRepository);
    }

    @Test
    void shouldAuthenticateUserWithValidToken() throws Exception {
        String email = "student@example.com";

        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.extractEmail("valid-token"))
                .thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(user.getEmail()).thenReturn(email);
        when(user.isEnabled()).thenReturn(true);

        // Obtain the STUDENT enum value without assuming its package.
        Class<?> roleType =
                User.class.getMethod("getRole").getReturnType();

        @SuppressWarnings({"rawtypes", "unchecked"})
        Object studentRole = Enum.valueOf(
                (Class<? extends Enum>) roleType,
                "STUDENT"
        );

        doReturn(studentRole).when(user).getRole();

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(email, authentication.getPrincipal());
        assertTrue(
                authentication.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_STUDENT"))
        );

        verify(userRepository).findByEmail(email);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenUserDoesNotExist() throws Exception {
        String email = "missing@example.com";

        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.extractEmail("valid-token"))
                .thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateWhenUserIsDisabled() throws Exception {
        String email = "disabled@example.com";

        request.addHeader("Authorization", "Bearer valid-token");

        when(jwtService.extractEmail("valid-token"))
                .thenReturn(email);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(user.isEnabled()).thenReturn(false);

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
        verify(user, never()).getRole();
    }

    @Test
    void shouldClearSecurityContextWhenTokenIsInvalid() throws Exception {
        request.addHeader("Authorization", "Bearer invalid-token");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "old-user",
                        null,
                        java.util.List.of()
                )
        );

        when(jwtService.extractEmail("invalid-token"))
                .thenThrow(new IllegalArgumentException("Invalid token"));

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertNull(
                SecurityContextHolder.getContext().getAuthentication()
        );

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldNotAuthenticateAgainWhenAlreadyAuthenticated() throws Exception {
        request.addHeader("Authorization", "Bearer valid-token");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "existing-user",
                        null,
                        java.util.List.of()
                )
        );

        when(jwtService.extractEmail("valid-token"))
                .thenReturn("student@example.com");

        jwtAuthenticationFilter.doFilter(request, response, filterChain);

        assertEquals(
                "existing-user",
                SecurityContextHolder.getContext()
                        .getAuthentication().getPrincipal()
        );

        verifyNoInteractions(userRepository);
        verify(filterChain).doFilter(request, response);
    }
}
