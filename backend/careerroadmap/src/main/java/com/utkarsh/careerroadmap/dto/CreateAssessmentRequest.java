
package com.utkarsh.careerroadmap.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateAssessmentRequest {

    @NotBlank(message = "Assessment title is required")
    @Size(max = 150)
    private String title;

    @Size(max = 1000)
    private String description;

    @Size(max = 100)
    private String category;

    @NotNull(message = "Total marks are required")
    @Min(value = 1, message = "Total marks must be positive")
    private Integer totalMarks;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be positive")
    private Integer durationMinutes;
}
