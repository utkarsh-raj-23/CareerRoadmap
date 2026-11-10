package com.utkarsh.careerroadmap.dto;

import com.utkarsh.careerroadmap.enums.SkillLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CareerSkillGapResponse {

    private Long skillId;
    private String skillName;

    private SkillLevel currentLevel;
    private SkillLevel requiredLevel;

    private boolean mandatory;
}