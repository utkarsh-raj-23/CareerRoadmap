package com.utkarsh.careerroadmap.controller;

import com.utkarsh.careerroadmap.dto.ResumeAnalysisResponse;
import com.utkarsh.careerroadmap.service.ResumeAnalysisService;
import com.utkarsh.careerroadmap.service.StudentSkillService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/resumes")
public class ResumeAnalysisController {

    private final ResumeAnalysisService resumeAnalysisService;
    private final StudentSkillService studentSkillService;

    public ResumeAnalysisController(
            ResumeAnalysisService resumeAnalysisService,
            StudentSkillService studentSkillService) {

        this.resumeAnalysisService = resumeAnalysisService;
        this.studentSkillService = studentSkillService;
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        // 1. Analyze resume using Python AI service.
        ResumeAnalysisResponse response =
                resumeAnalysisService.analyzeResume(id, email);

        // 2. Save extracted skills into MySQL.
        studentSkillService.saveExtractedSkills(
                email,
                response.getSkills_analysis()
        );

        // 3. Return the analysis response.
        return ResponseEntity.ok(response);
    }
}