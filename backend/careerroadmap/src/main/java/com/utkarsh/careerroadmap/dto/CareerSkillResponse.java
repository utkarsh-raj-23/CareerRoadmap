
package com.utkarsh.careerroadmap.dto;

import com.utkarsh.careerroadmap.enums.SkillLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CareerSkillResponse {

    private Long skillId;
    private String skillName;
    private String category;
    private SkillLevel requiredLevel;
    private boolean mandatory;
}
