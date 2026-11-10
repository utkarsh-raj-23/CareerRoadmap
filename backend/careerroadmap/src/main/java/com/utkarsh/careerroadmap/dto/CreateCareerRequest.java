
package com.utkarsh.careerroadmap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCareerRequest {

    @NotBlank(message = "Career name is required")
    @Size(max = 150, message = "Career name cannot exceed 150 characters")
    private String name;

    @Size(max = 100, message = "Category cannot exceed 100 characters")
    private String category;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;
}
