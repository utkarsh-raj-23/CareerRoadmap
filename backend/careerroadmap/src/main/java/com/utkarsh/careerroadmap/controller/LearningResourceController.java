package com.utkarsh.careerroadmap.controller;

import com.utkarsh.careerroadmap.dto.CreateLearningResourceRequest;
import com.utkarsh.careerroadmap.dto.LearningResourceResponse;

import com.utkarsh.careerroadmap.service.LearningResourceService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class LearningResourceController {

    private final LearningResourceService resourceService;

    public LearningResourceController(
            LearningResourceService resourceService) {

        this.resourceService = resourceService;
    }

    @PostMapping
    public ResponseEntity<LearningResourceResponse> createResource(
            @Valid @RequestBody CreateLearningResourceRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(resourceService.createResource(request));
    }

    @GetMapping
    public ResponseEntity<List<LearningResourceResponse>> getAllResources() {

        return ResponseEntity.ok(
                resourceService.getAllResources()
        );
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<List<LearningResourceResponse>> getResourcesBySkill(
            @PathVariable String title,
            @RequestParam(defaultValue = "false") boolean freeOnly) {

        return ResponseEntity.ok(
                resourceService.getResourcesBySkill(
                        title,
                        freeOnly
                )
        );
    }

    @GetMapping("/search")
    public ResponseEntity<List<LearningResourceResponse>> searchByTitle(
            @RequestParam String title,
            @RequestParam(defaultValue = "false") boolean freeOnly) {

        return ResponseEntity.ok(
                resourceService.searchByTitle(title, freeOnly)
        );
    }
}