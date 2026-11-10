
package com.utkarsh.careerroadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CareerResponse {

    private Long id;
    private String name;
    private String category;
    private String description;
    private List<CareerSkillResponse> requiredSkills;
}
