package com.utkarsh.careerroadmap.dto;

import com.utkarsh.careerroadmap.enums.SkillLevel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSkillRequest {

    @NotNull(message = "Proficiency level is required")
    private SkillLevel proficiencyLevel;

    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 50, message = "Experience cannot exceed 50 years")
    private Integer yearsOfExperience;
}