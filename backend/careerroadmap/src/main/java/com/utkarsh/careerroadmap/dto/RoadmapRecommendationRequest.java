package com.utkarsh.careerroadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoadmapRecommendationRequest {

    private String target_career;
    private List<String> current_skills;
    private String experience_level;
    private Integer duration_weeks;
}
