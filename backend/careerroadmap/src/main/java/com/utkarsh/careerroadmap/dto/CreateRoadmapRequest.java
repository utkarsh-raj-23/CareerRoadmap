package com.utkarsh.careerroadmap.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateRoadmapRequest {

    @NotNull(message = "Career ID is required")
    private Long careerId;
}