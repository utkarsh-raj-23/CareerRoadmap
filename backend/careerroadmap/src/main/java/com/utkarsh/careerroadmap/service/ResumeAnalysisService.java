package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.ResumeAnalysisResponse;
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

    public ResumeAnalysisResponse analyzeResume(
            Long resumeId,
            String email) {

        // 1. Find the resume belonging to the logged-in user.
        Resume resume = resumeRepository
                .findByIdAndUser_Email(resumeId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        NOT_FOUND, "Resume not found"
                ));

        try {
            // 2. Read the saved PDF.
            Path filePath = Path.of(resume.getStoredFileName());

            if (filePath.toFile().exists()) {
                throw new ResponseStatusException(
                        NOT_FOUND, "Resume file not found on server"
                );
            }

            byte[] fileBytes = Files.readAllBytes(filePath);

            // 3. Prepare the PDF as multipart form data.
            ByteArrayResource fileResource =
                    new ByteArrayResource(fileBytes) {

                        @Override
                        public String getFilename() {
                            return resume.getOriginalFileName();
                        }
                    };

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(MediaType.APPLICATION_PDF);

            HttpEntity<ByteArrayResource> fileEntity =
                    new HttpEntity<>(fileResource, fileHeaders);

            MultiValueMap<String, Object> body =
                    new LinkedMultiValueMap<>();

            body.add("file", fileEntity);

            // 4. Send the PDF to Python and receive its response.
            return aiRestClient.post()
                    .uri("/analyze-resume")
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(ResumeAnalysisResponse.class);

        } catch (IOException exception) {

            throw new ResponseStatusException(
                    INTERNAL_SERVER_ERROR,
                    "Unable to read resume file"
            );

        } catch (RestClientException exception) {

            throw new ResponseStatusException(
                    BAD_GATEWAY,
                    "Unable to communicate with Python AI service"
            );
        }
    }
}