package com.utkarsh.careerroadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CareerRecommendationResponse {

    private Long careerId;
    private String careerName;
    private String category;
    private String description;

    private double matchPercentage;

    private int totalRequiredSkills;
    private int matchedSkillsCount;

    private List<String> matchedSkills;
    private List<String> missingSkills;

    private List<CareerSkillGapResponse> proficiencyGaps;
}