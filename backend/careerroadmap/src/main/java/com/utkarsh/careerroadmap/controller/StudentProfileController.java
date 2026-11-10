
package com.utkarsh.careerroadmap.controller;

import com.utkarsh.careerroadmap.dto.StudentProfileRequest;
import com.utkarsh.careerroadmap.dto.StudentProfileResponse;

import com.utkarsh.careerroadmap.service.StudentProfileService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class StudentProfileController {

    private final StudentProfileService profileService;

    public StudentProfileController(
            StudentProfileService profileService) {

        this.profileService = profileService;
    }

    // Create student profile
    @PostMapping("/me")
    public ResponseEntity<StudentProfileResponse> createProfile(
            @Valid @RequestBody StudentProfileRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        StudentProfileResponse response =
                profileService.createProfile(email, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get logged-in student's profile
    @GetMapping("/me")
    public ResponseEntity<StudentProfileResponse> getProfile(
            Authentication authentication) {

        String email = authentication.getName();

        StudentProfileResponse response =
                profileService.getProfile(email);

        return ResponseEntity.ok(response);
    }

    // Update logged-in student's profile
    @PutMapping("/me")
    public ResponseEntity<StudentProfileResponse> updateProfile(
            @Valid @RequestBody StudentProfileRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        StudentProfileResponse response =
                profileService.updateProfile(email, request);

        return ResponseEntity.ok(response);
    }
}
