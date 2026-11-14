package com.utkarsh.careerroadmap.controller;

import com.utkarsh.careerroadmap.dto.CareerRecommendationResponse;
import com.utkarsh.careerroadmap.dto.SemanticSkillAnalysisResponse;
import com.utkarsh.careerroadmap.service.CareerRecommendationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class CareerRecommendationController {

    private final CareerRecommendationService recommendationService;

    public CareerRecommendationController(
            CareerRecommendationService recommendationService) {

        this.recommendationService = recommendationService;
    }

    @GetMapping("/careers")
    public ResponseEntity<List<CareerRecommendationResponse>>
    getCareerRecommendations(Authentication authentication) {

        return ResponseEntity.ok(
                recommendationService.getRecommendations(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/careers/{careerId}/skill-gaps")
    public ResponseEntity<CareerRecommendationResponse> getSkillGaps(
            @PathVariable Long careerId,
            Authentication authentication) {

        return ResponseEntity.ok(
                recommendationService.getSkillGap(
                        careerId,
                        authentication.getName()
                )
        );
    }

    /**
     * FEATURE 3: Deep Semantic Skill Analysis & Gap Breakdown for a specific career path.
     */
    @GetMapping("/careers/{careerId}/semantic-analysis")
    public ResponseEntity<SemanticSkillAnalysisResponse> getSemanticSkillAnalysis(
            @PathVariable Long careerId,
            Authentication authentication) {

        return ResponseEntity.ok(
                recommendationService.getSemanticSkillAnalysis(
                        careerId,
                        authentication.getName()
                )
        );
    }
}