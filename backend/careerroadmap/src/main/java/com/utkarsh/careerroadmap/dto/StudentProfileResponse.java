
package com.utkarsh.careerroadmap.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StudentProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String college;
    private String degree;
    private String branch;
    private Integer graduationYear;
    private Integer currentSemester;
    private String bio;
    private String githubUrl;
    private String linkedinUrl;
}
