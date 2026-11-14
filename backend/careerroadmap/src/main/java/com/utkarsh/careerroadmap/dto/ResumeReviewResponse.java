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
public class ResumeReviewResponse {

    private Integer ats_score;
    private String target_career;
    private String overall_summary;
    private List<String> strengths;
    private List<String> areas_for_improvement;
    private Map<String, String> section_feedback;
    private Integer formatting_and_layout_score;
    private Integer impact_and_metrics_score;
    private List<String> missing_keywords;
    private List<String> actionable_recommendations;
    private String engine;
    private String filename;
    private Integer total_pages;
}
