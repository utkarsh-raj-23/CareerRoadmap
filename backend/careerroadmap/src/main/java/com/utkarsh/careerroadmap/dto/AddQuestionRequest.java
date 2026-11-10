
package com.utkarsh.careerroadmap.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AddQuestionRequest {

    @NotBlank(message = "Question text is required")
    @Size(max = 2000)
    private String questionText;

    @NotNull(message = "Marks are required")
    @Min(value = 1, message = "Marks must be positive")
    private Integer marks;

    @NotEmpty(message = "At least two options are required")
    @Size(min = 2, message = "At least two options are required")
    private List<@NotBlank String> options;

    @NotNull(message = "Correct option index is required")
    @Min(value = 0, message = "Correct option index cannot be negative")
    private Integer correctOptionIndex;
}
