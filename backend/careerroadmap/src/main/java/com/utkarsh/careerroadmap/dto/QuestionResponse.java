
package com.utkarsh.careerroadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class QuestionResponse {

    private Long id;
    private String questionText;
    private Integer marks;
    private Integer orderIndex;
    private List<QuestionOptionResponse> options;
}
