package com.utkarsh.careerroadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminDashboardResponse {

    private long totalUsers;
    private long totalCareers;
    private long totalSkills;
    private long totalAssessments;
    private long totalLearningResources;
}