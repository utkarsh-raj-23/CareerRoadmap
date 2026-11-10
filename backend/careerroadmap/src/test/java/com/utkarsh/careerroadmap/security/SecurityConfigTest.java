
package com.utkarsh.careerroadmap.security;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.security.test.context.support.WithMockUser;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void passwordEncoderShouldEncodePassword() {
        String password = "password123";

        String encodedPassword =
                passwordEncoder.encode(password);

        assertNotNull(encodedPassword);
        assertNotEquals(password, encodedPassword);
        assertTrue(passwordEncoder.matches(password, encodedPassword));
    }

    @Test
    void passwordEncoderShouldRejectIncorrectPassword() {
        String encodedPassword =
                passwordEncoder.encode("password123");

        assertFalse(
                passwordEncoder.matches("wrongpassword", encodedPassword)
        );
    }

    @Test
    void unauthenticatedUserShouldNotAccessProfileApi() throws Exception {
        mockMvc.perform(get("/api/profile/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthenticatedUserShouldNotAccessAdminApi() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentShouldNotAccessAdminDashboard() throws Exception {
        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentShouldNotCreateCareer() throws Exception {
        mockMvc.perform(
                post("/api/careers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentShouldNotCreateAssessment() throws Exception {
        mockMvc.perform(
                post("/api/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentShouldNotCreateLearningResource() throws Exception {
        mockMvc.perform(
                post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isForbidden());
    }

    @Test
    void loginEndpointShouldBePublic() throws Exception {
        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(result ->
                assertNotEquals(
                        401,
                        result.getResponse().getStatus(),
                        "Login endpoint should not require authentication"
                )
        );
    }

    @Test
    void registerEndpointShouldBePublic() throws Exception {
        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(result ->
                assertNotEquals(
                        401,
                        result.getResponse().getStatus(),
                        "Register endpoint should not require authentication"
                )
        );
    }
}
