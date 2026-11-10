package com.utkarsh.careerroadmap.controller;

import com.utkarsh.careerroadmap.dto.AddSkillRequest;
import com.utkarsh.careerroadmap.dto.UpdateSkillRequest;
import com.utkarsh.careerroadmap.dto.StudentSkillResponse;

import com.utkarsh.careerroadmap.service.StudentSkillService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
public class StudentSkillController {

    private final StudentSkillService studentSkillService;

    public StudentSkillController(
            StudentSkillService studentSkillService) {

        this.studentSkillService = studentSkillService;
    }

    // Get all skills of logged-in student.
    @GetMapping("/me")
    public ResponseEntity<List<StudentSkillResponse>> getMySkills(
            Authentication authentication) {

        return ResponseEntity.ok(
                studentSkillService.getMySkills(
                        authentication.getName()
                )
        );
    }

    // Manually add a skill.
    @PostMapping("/me")
    public ResponseEntity<StudentSkillResponse> addSkill(
            @Valid @RequestBody AddSkillRequest request,
            Authentication authentication) {

        StudentSkillResponse response =
                studentSkillService.addSkill(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Update proficiency and experience.
    @PutMapping("/me/{id}")
    public ResponseEntity<StudentSkillResponse> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody UpdateSkillRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                studentSkillService.updateSkill(
                        authentication.getName(),
                        id,
                        request
                )
        );
    }

    // Delete a skill.
    @DeleteMapping("/me/{id}")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable Long id,
            Authentication authentication) {

        studentSkillService.deleteSkill(
                authentication.getName(),
                id
        );

        return ResponseEntity.noContent().build();
    }
}