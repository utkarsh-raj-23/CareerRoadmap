package com.utkarsh.careerroadmap.controller;

import com.cloudinary.api.exceptions.ApiException;
import com.utkarsh.careerroadmap.dto.ResumeAnalysisResponse;
import com.utkarsh.careerroadmap.dto.ResumeReviewRequest;
import com.utkarsh.careerroadmap.dto.ResumeReviewResponse;
import com.utkarsh.careerroadmap.dto.SemanticSkillAnalysisResponse;
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

    /**
     * Standard resume analysis & automatic skill saving.
     */
    @PostMapping("/{id}/analyze")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(
            @PathVariable Long id,
            Authentication authentication) {

        String email = authentication.getName();

        // 1. Analyze resume using Python AI service.
        ResumeAnalysisResponse response =
                resumeAnalysisService.analyzeResume(id, email);
    // 2. Save extracted skills into database.
    studentSkillService.saveExtractedSkills(
            email,
            response.getSkills_analysis()
    );

        // 3. Return the analysis response.
        return ResponseEntity.ok(response);
    }

    /**
     * FEATURE 1: Deep AI Resume Review & ATS Scoring for a saved resume.
     */
    @PostMapping("/{id}/review")
    public ResponseEntity<ResumeReviewResponse> reviewResume(
            @PathVariable Long id,
            @RequestParam(required = false) String targetCareer,
            Authentication authentication) {

        String email = authentication.getName();
        ResumeReviewResponse response =
                resumeAnalysisService.reviewResume(id, email, targetCareer);

        return ResponseEntity.ok(response);
    }

    /**
     * FEATURE 1: AI Resume Review from plain text.
     */
    @PostMapping("/review-text")
    public ResponseEntity<ResumeReviewResponse> reviewResumeText(
            @RequestBody ResumeReviewRequest request) {

        ResumeReviewResponse response =
                resumeAnalysisService.reviewResumeText(request);

        return ResponseEntity.ok(response);
    }

    /**
     * FEATURE 3: Deep Semantic Skill Analysis directly from a stored resume.
     */
    @PostMapping("/{id}/skill-analysis")
    public ResponseEntity<SemanticSkillAnalysisResponse> analyzeSkillsFromResume(
            @PathVariable Long id,
            @RequestParam(required = false) String targetCareer,
            Authentication authentication) {

        String email = authentication.getName();
        SemanticSkillAnalysisResponse response =
                resumeAnalysisService.analyzeSkillsForResume(id, email, targetCareer);

        return ResponseEntity.ok(response);
    }
}
