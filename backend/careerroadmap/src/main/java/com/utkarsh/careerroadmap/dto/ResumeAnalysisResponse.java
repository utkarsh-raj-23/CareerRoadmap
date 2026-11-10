package com.utkarsh.careerroadmap.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResumeAnalysisResponse {

    private String filename;

    private int total_pages;

    private String extracted_text;

    private SkillsAnalysisResponse skills_analysis;
}