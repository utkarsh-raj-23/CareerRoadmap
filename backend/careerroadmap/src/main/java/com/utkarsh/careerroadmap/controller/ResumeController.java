
package com.utkarsh.careerroadmap.controller;

import com.utkarsh.careerroadmap.dto.ResumeResponse;
import com.utkarsh.careerroadmap.service.ResumeService;

import jakarta.validation.constraints.NotNull;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    // Upload resume
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ResumeResponse> uploadResume(
            @RequestParam("file") @NotNull MultipartFile file,
            Authentication authentication) {

        String email = authentication.getName();

        ResumeResponse response =
                resumeService.uploadResume(email, file);

        return ResponseEntity.ok(response);
    }

    // Get all resumes of logged-in user
    @GetMapping
    public ResponseEntity<List<ResumeResponse>> getMyResumes(
            Authentication authentication) {

        String email = authentication.getName();

        return ResponseEntity.ok(
                resumeService.getMyResumes(email)
        );
    }

    // Download resume
    @GetMapping("/{id}")
    public ResponseEntity<Resource> downloadResume(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        Resource resource =
                resumeService.downloadResume(id, email);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"resume.pdf\""
                )
                .body(resource);
    }


    // Delete resume
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResume(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        resumeService.deleteResume(id, email);

        return ResponseEntity.noContent().build();
    }

}
