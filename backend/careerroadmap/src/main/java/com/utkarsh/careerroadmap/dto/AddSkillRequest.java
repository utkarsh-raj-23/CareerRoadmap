package com.utkarsh.careerroadmap.dto;

import com.utkarsh.careerroadmap.enums.SkillLevel;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddSkillRequest {

    @NotBlank(message = "Skill name is required")
    @Size(max = 100, message = "Skill name cannot exceed 100 characters")
    private String skillName;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    private SkillLevel proficiencyLevel;

    @Min(value = 0, message = "Experience cannot be negative")
    @Max(value = 50, message = "Experience cannot exceed 50 years")
    private Integer yearsOfExperience;
}