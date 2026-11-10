package com.utkarsh.careerroadmap.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SkillsAnalysisResponse {

    private int total_skills_found;

    private List<String> all_skills;

    private Map<String, List<String>> categorized_skills;
}