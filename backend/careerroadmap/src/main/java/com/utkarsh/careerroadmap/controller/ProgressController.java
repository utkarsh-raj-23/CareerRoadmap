package com.utkarsh.careerroadmap.controller;

import com.utkarsh.careerroadmap.dto.ProgressSummaryResponse;

import com.utkarsh.careerroadmap.service.ProgressService;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ProgressSummaryResponse> getOverallProgress(
            Authentication authentication) {

        return ResponseEntity.ok(
                progressService.getOverallProgress(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/roadmaps/{roadmapId}")
    public ResponseEntity<ProgressSummaryResponse> getRoadmapProgress(
            @PathVariable Long roadmapId,
            Authentication authentication) {

        return ResponseEntity.ok(
                progressService.getRoadmapProgress(
                        roadmapId,
                        authentication.getName()
                )
        );
    }
}