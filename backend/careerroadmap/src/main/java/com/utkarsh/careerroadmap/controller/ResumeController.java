
package com.utkarsh.careerroadmap.controller;

import com.utkarsh.careerroadmap.dto.ResumeResponse;
import com.utkarsh.careerroadmap.service.ResumeService;

import jakarta.validation.constraints.NotNull;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    // Upload resume
    @PostMapping(
            value = "/resume/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam("file") @NotNull MultipartFile file,
            Authentication authentication) throws IOException {

        String email = authentication.getName();

        ResumeResponse response =
                resumeService.uploadResume(email, file);

        return ResponseEntity.ok(response);
    }

    // Get all resumes of logged-in user
    @GetMapping("my/resume")
    public ResponseEntity<List<ResumeResponse>> getMyResumes(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                resumeService.getMyResumes(email)
        );
    }

    // Download resume
    @GetMapping("/resume/download")
    public ResponseEntity<Resource> downloadResume(
            @RequestParam String fileUrl) throws IOException {

        URL url = new URL(fileUrl);

        InputStreamResource resource =
                new InputStreamResource(url.openStream());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=resume.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }


}
