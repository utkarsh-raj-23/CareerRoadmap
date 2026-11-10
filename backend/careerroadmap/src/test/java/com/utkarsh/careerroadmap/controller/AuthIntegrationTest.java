
package com.utkarsh.careerroadmap.controller;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String generateEmail() {
        return "test_" + UUID.randomUUID() + "@example.com";
    }

    private String registerUser(String email) throws Exception {

        String request = """
                {
                    "fullName": "Test Student",
                    "email": "%s",
                    "password": "Password@123"
                }
                """.formatted(email);

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isCreated());

        return email;
    }

    private String loginUser(String email) throws Exception {

        String request = """
                {
                    "email": "%s",
                    "password": "Password@123"
                }
                """.formatted(email);

        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return json.get("token").asText();
    }

    @Test
    void registerShouldCreateNewUser() throws Exception {

        String email = generateEmail();

        String request = """
                {
                    "fullName": "Test Student",
                    "email": "%s",
                    "password": "Password@123"
                }
                """.formatted(email);

        String response = mockMvc.perform(
                        post("/api/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message")
                        .value("Registration successful"))
                .andExpect(jsonPath("$.email").value(email))
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertNotNull(response);
    }

    @Test
    void registerShouldRejectDuplicateEmail() throws Exception {

        String email = generateEmail();

        registerUser(email);

        String request = """
                {
                    "fullName": "Another Student",
                    "email": "%s",
                    "password": "Password@123"
                }
                """.formatted(email);

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isConflict());
    }

    @Test
    void loginShouldReturnJwtToken() throws Exception {

        String email = generateEmail();

        registerUser(email);

        String request = """
                {
                    "email": "%s",
                    "password": "Password@123"
                }
                """.formatted(email);

        String response = mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message")
                        .value("Login successful"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.fullName")
                        .value("Test Student"))
                .andExpect(jsonPath("$.token").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        assertFalse(json.get("token").asText().isEmpty());
    }

    @Test
    void loginShouldRejectIncorrectPassword() throws Exception {

        String email = generateEmail();

        registerUser(email);

        String request = """
                {
                    "email": "%s",
                    "password": "WrongPassword@123"
                }
                """.formatted(email);

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void loginShouldRejectUnregisteredEmail() throws Exception {

        String request = """
                {
                    "email": "unknown@example.com",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(
                post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void validJwtShouldAllowAccessToCurrentUserEndpoint()
            throws Exception {

        String email = generateEmail();

        registerUser(email);

        String token = loginUser(email);

        mockMvc.perform(
                        get("/api/auth/me")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.authorities[0]")
                        .value("ROLE_STUDENT"));
    }

    @Test
    void currentUserEndpointShouldRejectMissingToken()
            throws Exception {

        mockMvc.perform(
                get("/api/auth/me")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void currentUserEndpointShouldRejectInvalidToken()
            throws Exception {

        mockMvc.perform(
                get("/api/auth/me")
                        .header("Authorization", "Bearer invalid.token.here")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void currentUserEndpointShouldRejectMalformedAuthorizationHeader()
            throws Exception {

        mockMvc.perform(
                get("/api/auth/me")
                        .header("Authorization", "Basic abc123")
        ).andExpect(status().isUnauthorized());
    }

    @Test
    void registrationShouldRejectInvalidEmail()
            throws Exception {

        String request = """
                {
                    "fullName": "Test Student",
                    "email": "invalid-email",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(
                post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request)
        ).andExpect(status().isBadRequest());
    }
}
