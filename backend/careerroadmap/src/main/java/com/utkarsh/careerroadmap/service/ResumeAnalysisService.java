package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.*;
import com.utkarsh.careerroadmap.entity.Resume;
import com.utkarsh.careerroadmap.repository.ResumeRepository;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.springframework.http.HttpStatus.*;

@Service
public class ResumeAnalysisService {

    private final ResumeRepository resumeRepository;
    private final RestClient aiRestClient;

    public ResumeAnalysisService(
            ResumeRepository resumeRepository,
            RestClient aiRestClient) {

        this.resumeRepository = resumeRepository;
        this.aiRestClient = aiRestClient;
    }

    /**
     * Safely reads the PDF bytes whether stored locally on disk or hosted on Cloudinary (HTTPS).
     * Resolves the Cloudinary URL vs local file path technical debt.
     */
    public byte[] getResumeBytes(Resume resume) throws IOException {
        String storedPath = resume.getStoredFileName();
        if (storedPath == null || storedPath.isBlank()) {
            throw new ResponseStatusException(NOT_FOUND, "Resume file reference is missing");
        }

        if (storedPath.startsWith("http://") || storedPath.startsWith("https://")) {
            URL url = URI.create(storedPath).toURL();
            try (InputStream in = url.openStream()) {
                return in.readAllBytes();
            }
        } else {
            Path path = Path.of(storedPath);
            if (!Files.exists(path)) {
                throw new ResponseStatusException(NOT_FOUND, "Resume file not found on server");
            }
            return Files.readAllBytes(path);
        }
    }

    private HttpEntity<ByteArrayResource> createPdfEntity(byte[] fileBytes, String originalFileName) {
        ByteArrayResource fileResource = new ByteArrayResource(fileBytes) {
            @Override
            public String getFilename() {
                return (originalFileName != null && !originalFileName.isBlank()) ? originalFileName : "resume.pdf";
            }
        };

        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.APPLICATION_PDF);
        return new HttpEntity<>(fileResource, fileHeaders);
    }

    /**
     * Standard resume analysis & skill extraction.
     */
    public ResumeAnalysisResponse analyzeResume(Long resumeId, String email) {
        Resume resume = resumeRepository
                .findByIdAndUser_Email(resumeId, email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Resume not found"));

        try {
            byte[] fileBytes = getResumeBytes(resume);
            HttpEntity<ByteArrayResource> fileEntity = createPdfEntity(fileBytes, resume.getOriginalFileName());

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileEntity);

            return aiRestClient.post()
                    .uri("/analyze-resume")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(ResumeAnalysisResponse.class);

        } catch (IOException exception) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Unable to read resume file: " + exception.getMessage());
        } catch (RestClientException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "Unable to communicate with AI service: " + exception.getMessage());
        }
    }

    /**
     * FEATURE 1: Deep AI Resume Review & ATS Scoring from an uploaded resume.
     */
    public ResumeReviewResponse reviewResume(Long resumeId, String email, String targetCareer) {
        Resume resume = resumeRepository
                .findByIdAndUser_Email(resumeId, email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Resume not found"));

        try {
            byte[] fileBytes = getResumeBytes(resume);
            HttpEntity<ByteArrayResource> fileEntity = createPdfEntity(fileBytes, resume.getOriginalFileName());

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileEntity);
            if (targetCareer != null && !targetCareer.isBlank()) {
                body.add("target_career", targetCareer);
            }

            return aiRestClient.post()
                    .uri("/resume-review/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(ResumeReviewResponse.class);

        } catch (IOException exception) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Unable to read resume file: " + exception.getMessage());
        } catch (RestClientException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "Unable to communicate with AI service: " + exception.getMessage());
        }
    }

    /**
     * FEATURE 1 (Text): Deep AI Resume Review from raw text.
     */
    public ResumeReviewResponse reviewResumeText(ResumeReviewRequest request) {
        try {
            return aiRestClient.post()
                    .uri("/resume-review")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(ResumeReviewResponse.class);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "Unable to communicate with AI service: " + exception.getMessage());
        }
    }

    /**
     * FEATURE 2: Dynamic Roadmap Recommendation from student skills & career goal.
     */
    public RoadmapRecommendationResponse recommendRoadmap(RoadmapRecommendationRequest request) {
        try {
            return aiRestClient.post()
                    .uri("/roadmap-recommendation")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(RoadmapRecommendationResponse.class);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "Unable to communicate with AI service: " + exception.getMessage());
        }
    }

    /**
     * FEATURE 3: Deep Semantic Skill Analysis & Gap Breakdown from text / skill list.
     */
    public SemanticSkillAnalysisResponse analyzeSkills(SemanticSkillAnalysisRequest request) {
        try {
            return aiRestClient.post()
                    .uri("/skill-analysis")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(SemanticSkillAnalysisResponse.class);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "Unable to communicate with AI service: " + exception.getMessage());
        }
    }

    /**
     * FEATURE 3 (Resume): Deep Semantic Skill Analysis directly from a stored resume.
     */
    public SemanticSkillAnalysisResponse analyzeSkillsForResume(Long resumeId, String email, String targetCareer) {
        Resume resume = resumeRepository
                .findByIdAndUser_Email(resumeId, email)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Resume not found"));

        try {
            byte[] fileBytes = getResumeBytes(resume);
            HttpEntity<ByteArrayResource> fileEntity = createPdfEntity(fileBytes, resume.getOriginalFileName());

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileEntity);
            if (targetCareer != null && !targetCareer.isBlank()) {
                body.add("target_career", targetCareer);
            }

            return aiRestClient.post()
                    .uri("/skill-analysis/upload")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(SemanticSkillAnalysisResponse.class);

        } catch (IOException exception) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Unable to read resume file: " + exception.getMessage());
        } catch (RestClientException exception) {
            throw new ResponseStatusException(BAD_GATEWAY, "Unable to communicate with AI service: " + exception.getMessage());
        }
    }
}
