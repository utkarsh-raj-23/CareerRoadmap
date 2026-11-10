
package com.utkarsh.careerroadmap.controller;

import com.utkarsh.careerroadmap.dto.CreateCareerRequest;
import com.utkarsh.careerroadmap.dto.AddCareerSkillRequest;
import com.utkarsh.careerroadmap.dto.CareerResponse;
import com.utkarsh.careerroadmap.dto.CareerSkillResponse;

import com.utkarsh.careerroadmap.service.CareerService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/careers")
public class CareerController {

    private final CareerService careerService;

    public CareerController(CareerService careerService) {
        this.careerService = careerService;
    }

    // Create career
    @PostMapping
    public ResponseEntity<CareerResponse> createCareer(
            @Valid @RequestBody CreateCareerRequest request) {

        CareerResponse response =
                careerService.createCareer(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get all careers
    @GetMapping
    public ResponseEntity<List<CareerResponse>> getAllCareers() {

        return ResponseEntity.ok(
                careerService.getAllCareers()
        );
    }

    // Get career by ID
    @GetMapping("/{id}")
    public ResponseEntity<CareerResponse> getCareerById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                careerService.getCareerById(id)
        );
    }

    // Add skill to career
    @PostMapping("/{careerId}/skills")
    public ResponseEntity<CareerSkillResponse> addSkillToCareer(
            @PathVariable Long careerId,
            @Valid @RequestBody AddCareerSkillRequest request) {

        CareerSkillResponse response =
                careerService.addSkillToCareer(
                        careerId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Remove skill from career
    @DeleteMapping("/{careerId}/skills/{skillId}")
    public ResponseEntity<Void> removeSkillFromCareer(
            @PathVariable Long careerId,
            @PathVariable Long skillId) {

        careerService.removeSkillFromCareer(
                careerId,
                skillId
        );

        return ResponseEntity.noContent().build();
    }
}
