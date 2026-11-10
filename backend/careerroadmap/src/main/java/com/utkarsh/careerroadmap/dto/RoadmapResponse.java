package com.utkarsh.careerroadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class RoadmapResponse {

    private Long id;
    private String title;
    private Long careerId;
    private String careerName;
    private LocalDateTime createdAt;

    private List<RoadmapItemResponse> items;
}