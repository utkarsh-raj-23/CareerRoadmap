package com.utkarsh.careerroadmap.dto;

import com.utkarsh.careerroadmap.enums.SkillLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentSkillResponse {

    private Long id;

    private String skillName;

    private String category;

    private SkillLevel proficiencyLevel;

    private Integer yearsOfExperience;
}