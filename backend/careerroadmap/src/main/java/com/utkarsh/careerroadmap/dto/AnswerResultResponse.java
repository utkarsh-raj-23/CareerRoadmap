
package com.utkarsh.careerroadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AnswerResultResponse {

    private Long questionId;
    private String questionText;

    private String selectedOption;
    private String correctOption;

    private boolean correct;

    private Integer marksAwarded;
    private Integer totalQuestionMarks;
}
