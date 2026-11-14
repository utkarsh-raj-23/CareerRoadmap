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
public class RoadmapMilestoneDto {

    private Integer milestone_id;
    private String title;
    private List<String> topics;
    private String practical_task;
    private List<String> recommended_resources;
}
