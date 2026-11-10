
package com.utkarsh.careerroadmap.dto;

import com.utkarsh.careerroadmap.enums.SkillLevel;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCareerSkillRequest {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    @NotNull(message = "Required proficiency level is required")
    private SkillLevel requiredLevel;

    private boolean mandatory = true;
}
