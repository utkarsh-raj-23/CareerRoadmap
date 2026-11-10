
package com.utkarsh.careerroadmap.dto;

import com.utkarsh.careerroadmap.enums.AssessmentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class StudentAssessmentResponse {

    private Long id;
    private Long assessmentId;
    private String assessmentTitle;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer score;
    private AssessmentStatus status;
}
