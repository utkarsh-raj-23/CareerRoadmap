package com.utkarsh.careerroadmap.dto;

import com.utkarsh.careerroadmap.enums.ResourceType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LearningResourceResponse {

    private Long id;
    private Long skillId;
    private String skillName;
    private String title;
    private String description;
    private String url;
    private String provider;
    private ResourceType resourceType;
    private boolean free;
}