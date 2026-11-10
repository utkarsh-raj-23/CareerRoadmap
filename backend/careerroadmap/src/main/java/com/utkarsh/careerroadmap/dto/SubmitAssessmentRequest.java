
package com.utkarsh.careerroadmap.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SubmitAssessmentRequest {

    @NotNull(message = "Answers are required")
    @Size(max = 200, message = "Too many answers submitted")
    private List<@Valid AnswerSubmissionItem> answers;
}
