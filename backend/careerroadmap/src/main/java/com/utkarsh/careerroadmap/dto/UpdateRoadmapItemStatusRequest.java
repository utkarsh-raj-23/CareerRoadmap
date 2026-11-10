package com.utkarsh.careerroadmap.dto;

import com.utkarsh.careerroadmap.enums.RoadmapItemStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateRoadmapItemStatusRequest {

    @NotNull(message = "Status is required")
    private RoadmapItemStatus status;
}