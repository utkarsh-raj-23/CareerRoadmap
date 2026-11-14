package com.utkarsh.careerroadmap.controller;

import com.utkarsh.careerroadmap.dto.*;

import com.utkarsh.careerroadmap.service.RoadmapService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roadmaps")
public class RoadmapController {

    private final RoadmapService roadmapService;

    public RoadmapController(RoadmapService roadmapService) {
        this.roadmapService = roadmapService;
    }

    @PostMapping
    public ResponseEntity<RoadmapResponse> createRoadmap(
            @Valid @RequestBody CreateRoadmapRequest request,
            Authentication authentication) {

        RoadmapResponse response =
                roadmapService.createRoadmap(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<List<RoadmapResponse>> getMyRoadmaps(
            Authentication authentication) {

        return ResponseEntity.ok(
                roadmapService.getMyRoadmaps(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoadmapResponse> getRoadmap(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                roadmapService.getRoadmap(
                        id,
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/{roadmapId}/items/{itemId}/status")
    public ResponseEntity<RoadmapItemResponse> updateItemStatus(
            @PathVariable Long roadmapId,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateRoadmapItemStatusRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                roadmapService.updateItemStatus(
                        roadmapId,
                        itemId,
                        authentication.getName(),
                        request
                )
        );
    }

    /**
     * FEATURE 2: Generate dynamic AI roadmap recommendation for a specific career ID.
     */
    @PostMapping("/ai-recommend")
    public ResponseEntity<RoadmapRecommendationResponse> recommendAiRoadmap(
            @RequestParam Long careerId,
            @RequestParam(required = false, defaultValue = "Beginner") String experienceLevel,
            @RequestParam(required = false, defaultValue = "12") Integer durationWeeks,
            Authentication authentication) {

        RoadmapRecommendationResponse response =
                roadmapService.getAiRoadmapRecommendation(
                        authentication.getName(),
                        careerId,
                        experienceLevel,
                        durationWeeks
                );

        return ResponseEntity.ok(response);
    }

    /**
     * FEATURE 2: Generate dynamic AI roadmap with custom request body.
     */
    @PostMapping("/ai-generate")
    public ResponseEntity<RoadmapRecommendationResponse> generateAiRoadmap(
            @RequestBody RoadmapRecommendationRequest request,
            Authentication authentication) {

        RoadmapRecommendationResponse response =
                roadmapService.getAiRoadmapRecommendationDirect(
                        authentication.getName(),
                        request
                );

        return ResponseEntity.ok(response);
    }
}