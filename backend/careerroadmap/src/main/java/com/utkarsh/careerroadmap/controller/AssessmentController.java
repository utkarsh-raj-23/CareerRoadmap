
package com.utkarsh.careerroadmap.controller;

import com.utkarsh.careerroadmap.dto.*;

import com.utkarsh.careerroadmap.service.AssessmentService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assessments")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    // Create an assessment
    @PostMapping
    public ResponseEntity<AssessmentResponse> createAssessment(
            @Valid @RequestBody CreateAssessmentRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assessmentService.createAssessment(request));
    }

    // Get all active assessments
    @GetMapping
    public ResponseEntity<List<AssessmentResponse>> getAllAssessments() {

        return ResponseEntity.ok(
                assessmentService.getAllAssessments()
        );
    }

    // Add a question to an assessment
    @PostMapping("/{assessmentId}/questions")
    public ResponseEntity<QuestionResponse> addQuestion(
            @PathVariable Long assessmentId,
            @Valid @RequestBody AddQuestionRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assessmentService.addQuestion(
                        assessmentId,
                        request
                ));
    }

    // Get questions without correct answers
    @GetMapping("/{assessmentId}/questions")
    public ResponseEntity<List<QuestionResponse>> getQuestions(
            @PathVariable Long assessmentId) {

        return ResponseEntity.ok(
                assessmentService.getAssessmentQuestions(assessmentId)
        );
    }

    // Start an assessment attempt
    @PostMapping("/{assessmentId}/attempts")
    public ResponseEntity<StudentAssessmentResponse> startAssessment(
            @PathVariable Long assessmentId,
            Authentication authentication) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(assessmentService.startAssessment(
                        assessmentId,
                        authentication.getName()
                ));
    }

    // Get my assessment attempts
    @GetMapping("/my-attempts")
    public ResponseEntity<List<StudentAssessmentResponse>> getMyAttempts(
            Authentication authentication) {

        return ResponseEntity.ok(
                assessmentService.getMyAttempts(
                        authentication.getName()
                )
        );
    }

    // Submit answers and calculate the result
    @PostMapping("/attempts/{attemptId}/submit")
    public ResponseEntity<AssessmentResultResponse> submitAssessment(
            @PathVariable Long attemptId,
            @Valid @RequestBody SubmitAssessmentRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                assessmentService.submitAssessment(
                        attemptId,
                        authentication.getName(),
                        request
                )
        );
    }

    // Get detailed result of an attempt
    @GetMapping("/attempts/{attemptId}/result")
    public ResponseEntity<AssessmentResultResponse> getAssessmentResult(
            @PathVariable Long attemptId,
            Authentication authentication) {

        return ResponseEntity.ok(
                assessmentService.getAssessmentResult(
                        attemptId,
                        authentication.getName()
                )
        );
    }

}
