package com.utkarsh.careerroadmap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SemanticSkillAnalysisResponse {

    private String target_career;
    private Integer overall_match_percentage;
    private Integer total_skills_identified;
    private List<String> all_skills;
    private List<SkillWithProficiencyDto> skills_with_proficiency;
    private Map<String, List<String>> categorized_skills;
    private String strengths_summary;
    private List<MissingCriticalSkillDto> missing_critical_skills;
    private List<String> recommended_learning_priority;
    private String engine;
    private String notice;
}
