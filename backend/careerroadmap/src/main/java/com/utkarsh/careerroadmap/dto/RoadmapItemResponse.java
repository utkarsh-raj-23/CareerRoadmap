package com.utkarsh.careerroadmap.dto;

import com.utkarsh.careerroadmap.enums.RoadmapItemStatus;
import com.utkarsh.careerroadmap.enums.SkillLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RoadmapItemResponse {

    private Long id;
    private String skillName;
    private Integer orderIndex;

    private SkillLevel currentLevel;
    private SkillLevel targetLevel;

    private Integer estimatedHours;

    private RoadmapItemStatus status;
    private LocalDateTime completedAt;
}