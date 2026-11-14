package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.*;
import com.utkarsh.careerroadmap.entity.Resume;
import com.utkarsh.careerroadmap.entity.User;
import com.utkarsh.careerroadmap.repository.ResumeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ResumeAnalysisServiceAiTest {

    @Mock
    private ResumeRepository resumeRepository;

    private RestClient restClient;
    private ResumeAnalysisService resumeAnalysisService;

    @BeforeEach
    void setUp() {
        // Build real RestClient pointing to local AI test or default base URL
        restClient = RestClient.builder().baseUrl("http://localhost:8000").build();
        resumeAnalysisService = new ResumeAnalysisService(resumeRepository, restClient);
    }

    @Test
    void shouldReadLocalResumeBytesSuccessfully() throws IOException {
        Path tempFile = Files.createTempFile("test_resume", ".pdf");
        Files.write(tempFile, "%PDF-1.4\nSample content".getBytes());

        Resume resume = new Resume();
        resume.setStoredFileName(tempFile.toString());
        resume.setOriginalFileName("test_resume.pdf");

        byte[] bytes = resumeAnalysisService.getResumeBytes(resume);
        assertNotNull(bytes);
        assertTrue(bytes.length > 0);
        assertTrue(new String(bytes).startsWith("%PDF-1.4"));

        Files.deleteIfExists(tempFile);
    }

    @Test
    void shouldInstantiateAndMapResumeReviewResponseDto() {
        ResumeReviewResponse response = new ResumeReviewResponse();
        response.setAts_score(88);
        response.setTarget_career("Backend Engineer");
        response.setOverall_summary("Strong profile");
        response.setStrengths(List.of("Clean architecture", "Good metrics"));
        response.setAreas_for_improvement(List.of("Add unit testing"));
        response.setFormatting_and_layout_score(90);
        response.setImpact_and_metrics_score(85);
        response.setEngine("gemini-3.8-flash");

        assertEquals(88, response.getAts_score());
        assertEquals("Backend Engineer", response.getTarget_career());
        assertEquals("gemini-3.8-flash", response.getEngine());
        assertEquals(2, response.getStrengths().size());
    }

    @Test
    void shouldInstantiateAndMapRoadmapRecommendationResponseDto() {
        RoadmapMilestoneDto milestone = new RoadmapMilestoneDto(
                1,
                "OOP Foundations",
                List.of("Polymorphism", "SOLID"),
                "Build mini bank app",
                List.of("docs.oracle.com")
        );

        RoadmapPhaseDto phase = new RoadmapPhaseDto(
                1,
                "Phase 1: Core",
                4,
                "Master core concepts",
                List.of(milestone)
        );

        RoadmapRecommendationResponse response = new RoadmapRecommendationResponse();
        response.setTarget_career("Full Stack Developer");
        response.setTotal_weeks(12);
        response.setPhases(List.of(phase));
        response.setEngine("gemini-3.8-flash");

        assertEquals("Full Stack Developer", response.getTarget_career());
        assertEquals(12, response.getTotal_weeks());
        assertEquals(1, response.getPhases().size());
        assertEquals("Phase 1: Core", response.getPhases().get(0).getPhase_title());
    }

    @Test
    void shouldInstantiateAndMapSemanticSkillAnalysisResponseDto() {
        SemanticSkillAnalysisResponse response = new SemanticSkillAnalysisResponse();
        response.setTarget_career("Backend Developer");
        response.setOverall_match_percentage(85);
        response.setAll_skills(List.of("Java", "Spring Boot", "Docker"));
        response.setRecommended_learning_priority(List.of("Kubernetes", "Redis"));
        response.setEngine("gemini-3.8-flash");

        assertEquals("Backend Developer", response.getTarget_career());
        assertEquals(85, response.getOverall_match_percentage());
        assertEquals(3, response.getAll_skills().size());
        assertEquals("Kubernetes", response.getRecommended_learning_priority().get(0));
    }
}
