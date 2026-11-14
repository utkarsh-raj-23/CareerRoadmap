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
public class RoadmapPhaseDto {

    private Integer phase_number;
    private String phase_title;
    private Integer duration_weeks;
    private String objective;
    private List<RoadmapMilestoneDto> milestones;
}
