
package com.utkarsh.careerroadmap.dto;

import com.utkarsh.careerroadmap.enums.AssessmentStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class AssessmentResultResponse {

    private Long attemptId;

    private String assessmentTitle;

    private Integer score;
    private Integer totalMarks;

    private Double percentage;

    private AssessmentStatus status;

    private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    private List<AnswerResultResponse> answers;
}
