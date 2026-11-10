package com.utkarsh.careerroadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProgressSummaryResponse {

    private Long roadmapId;
    private String roadmapTitle;

    private int totalTasks;
    private int completedTasks;
    private int inProgressTasks;
    private int pendingTasks;

    private int totalEstimatedHours;
    private int completedEstimatedHours;

    private double completionPercentage;
}