
package com.utkarsh.careerroadmap.security;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RoleAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // 1. Unauthenticated user cannot access admin dashboard
    @Test
    void unauthenticatedUserShouldNotAccessAdminDashboard()
            throws Exception {

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    // 2. Student cannot access admin dashboard
    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void studentShouldNotAccessAdminDashboard()
            throws Exception {

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isForbidden());
    }

    // 3. Admin can access admin dashboard
    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void adminShouldAccessAdminDashboard()
            throws Exception {

        mockMvc.perform(get("/api/admin/dashboard"))
                .andExpect(status().isOk());
    }

    // 4. Student cannot create career
    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void studentShouldNotCreateCareer()
            throws Exception {

        mockMvc.perform(
                post("/api/careers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isForbidden());
    }

    // 5. Student cannot add skills to career
    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void studentShouldNotAddCareerSkill()
            throws Exception {

        mockMvc.perform(
                post("/api/careers/1/skills")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isForbidden());
    }

    // 6. Student cannot delete career skill
    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void studentShouldNotDeleteCareerSkill()
            throws Exception {

        mockMvc.perform(
                delete("/api/careers/1/skills/1")
        ).andExpect(status().isForbidden());
    }

    // 7. Student cannot create assessment
    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void studentShouldNotCreateAssessment()
            throws Exception {

        mockMvc.perform(
                post("/api/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isForbidden());
    }

    // 8. Student cannot add assessment questions
    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void studentShouldNotAddAssessmentQuestion()
            throws Exception {

        mockMvc.perform(
                post("/api/assessments/1/questions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isForbidden());
    }

    // 9. Student cannot create learning resources
    @Test
    @WithMockUser(username = "student@example.com", roles = "STUDENT")
    void studentShouldNotCreateLearningResource()
            throws Exception {

        mockMvc.perform(
                post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(status().isForbidden());
    }

    // 10. Admin is not blocked from career creation by security
    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void adminShouldNotBeForbiddenFromCreatingCareer()
            throws Exception {

        mockMvc.perform(
                post("/api/careers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(result ->
                org.junit.jupiter.api.Assertions.assertNotEquals(
                        403,
                        result.getResponse().getStatus()
                )
        );
    }

    // 11. Admin is not blocked from assessment creation by security
    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void adminShouldNotBeForbiddenFromCreatingAssessment()
            throws Exception {

        mockMvc.perform(
                post("/api/assessments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(result ->
                org.junit.jupiter.api.Assertions.assertNotEquals(
                        403,
                        result.getResponse().getStatus()
                )
        );
    }

    // 12. Admin is not blocked from resource creation by security
    @Test
    @WithMockUser(username = "admin@example.com", roles = "ADMIN")
    void adminShouldNotBeForbiddenFromCreatingResource()
            throws Exception {

        mockMvc.perform(
                post("/api/resources")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
        ).andExpect(result ->
                org.junit.jupiter.api.Assertions.assertNotEquals(
                        403,
                        result.getResponse().getStatus()
                )
        );
    }
}
