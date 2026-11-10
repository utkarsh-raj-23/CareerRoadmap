
package com.utkarsh.careerroadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AssessmentResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private Integer totalMarks;
    private Integer durationMinutes;
}
