package com.utkarsh.careerroadmap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class RoadmapRecommendationResponse {

    private String target_career;
    private String experience_level;
    private Integer total_weeks;
    private Integer estimated_hours_per_week;
    private Integer career_readiness_target_percentage;
    private String overview;
    private List<RoadmapPhaseDto> phases;
    private List<CapstoneProjectDto> capstone_projects;
    private List<String> certifications_or_assessments;
    private String engine;
    private String notice;
}
