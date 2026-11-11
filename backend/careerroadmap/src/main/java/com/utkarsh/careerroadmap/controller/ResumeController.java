
package com.utkarsh.careerroadmap.controller;

import com.utkarsh.careerroadmap.dto.ResumeResponse;
import com.utkarsh.careerroadmap.service.ResumeService;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
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
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
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
            Authentication authentication) throws IOException {

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

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadResume(
            @RequestParam String fileUrl) throws IOException {

        URL url = new URL(fileUrl);

        // Validate URL for security
        if (!"https".equalsIgnoreCase(url.getProtocol())
                || !"res.cloudinary.com".equalsIgnoreCase(url.getHost())) {
            return ResponseEntity.badRequest().build();
        }

        HttpURLConnection connection =
                (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);

        try {
            int status = connection.getResponseCode();

            if (status != HttpURLConnection.HTTP_OK) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        " downloaded failed. HTTP status: " + status
                );
            }

            byte[] pdfBytes;

            try (InputStream input =
                         connection.getInputStream()) {
                pdfBytes = input.readAllBytes();
            }

            // Verify PDF signature
            if (pdfBytes.length < 5
                    || pdfBytes[0] != '%'
                    || pdfBytes[1] != 'P'
                    || pdfBytes[2] != 'D'
                    || pdfBytes[3] != 'F'
                    || pdfBytes[4] != '-') {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Downloaded file is not a valid PDF"
                );
            }

            return ResponseEntity.ok()
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"resume.pdf\""
                    )
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfBytes.length)
                    .body(pdfBytes);

        } finally {
            connection.disconnect();
        }
    }

    @PutMapping(
            value = "/latest/update",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<ResumeResponse> updateLatestResume(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {

        String email = authentication.getName();

        ResumeResponse response =
                resumeService.updateLatestResume(email, file);

        return ResponseEntity.ok(response);
    }




}
