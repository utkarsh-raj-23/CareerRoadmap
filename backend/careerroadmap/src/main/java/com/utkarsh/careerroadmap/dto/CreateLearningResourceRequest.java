package com.utkarsh.careerroadmap.dto;

import com.utkarsh.careerroadmap.enums.ResourceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateLearningResourceRequest {

    @NotNull(message = "Skill ID is required")
    private Long skillId;

    @NotBlank(message = "Resource title is required")
    @Size(max = 200)
    private String title;

    @Size(max = 2000)
    private String description;

    @NotBlank(message = "Resource URL is required")
    @Size(max = 1000)
    private String url;

    @Size(max = 100)
    private String provider;

    @NotNull(message = "Resource type is required")
    private ResourceType resourceType;

    private boolean free = true;
}