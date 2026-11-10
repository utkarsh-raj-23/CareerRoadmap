
package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.CareerRecommendationResponse;
import com.utkarsh.careerroadmap.entity.Career;
import com.utkarsh.careerroadmap.entity.CareerSkill;
import com.utkarsh.careerroadmap.entity.Skill;
import com.utkarsh.careerroadmap.entity.StudentSkill;
import com.utkarsh.careerroadmap.entity.User;
import com.utkarsh.careerroadmap.enums.SkillLevel;
import com.utkarsh.careerroadmap.repository.CareerRepository;
import com.utkarsh.careerroadmap.repository.CareerSkillRepository;
import com.utkarsh.careerroadmap.repository.StudentSkillRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CareerRecommendationServiceTest {

    @Mock
    private CareerRepository careerRepository;

    @Mock
    private CareerSkillRepository careerSkillRepository;

    @Mock
    private StudentSkillRepository studentSkillRepository;

    @InjectMocks
    private CareerRecommendationService recommendationService;

    private Career career;
    private Skill java;
    private Skill spring;
    private Skill mysql;
    private User user;

    @BeforeEach
    void setUp() {

        career = new Career();
        career.setId(1L);
        career.setName("Java Backend Developer");
        career.setCategory("Software Development");
        career.setDescription("Build backend applications.");

        java = new Skill();
        java.setId(1L);
        java.setName("Java");
        java.setCategory("Programming");

        spring = new Skill();
        spring.setId(2L);
        spring.setName("Spring Boot");
        spring.setCategory("Backend");

        mysql = new Skill();
        mysql.setId(3L);
        mysql.setName("MySQL");
        mysql.setCategory("Database");

        user = new User();
        user.setId(10L);
        user.setEmail("test@gmail.com");
    }

    private CareerSkill createCareerSkill(
            Skill skill,
            SkillLevel requiredLevel) {

        CareerSkill careerSkill = new CareerSkill();
        careerSkill.setCareer(career);
        careerSkill.setSkill(skill);
        careerSkill.setRequiredLevel(requiredLevel);
        careerSkill.setMandatory(true);

        return careerSkill;
    }

    private StudentSkill createStudentSkill(
            Skill skill,
            SkillLevel currentLevel) {

        StudentSkill studentSkill = new StudentSkill();
        studentSkill.setUser(user);
        studentSkill.setSkill(skill);
        studentSkill.setProficiencyLevel(currentLevel);

        return studentSkill;
    }

    @Test
    void getRecommendationsShouldCalculateMatchPercentage() {

        when(careerRepository.findAll())
                .thenReturn(List.of(career));

        when(careerSkillRepository.findAllByCareer_Id(1L))
                .thenReturn(List.of(
                        createCareerSkill(java, SkillLevel.INTERMEDIATE),
                        createCareerSkill(spring, SkillLevel.INTERMEDIATE),
                        createCareerSkill(mysql, SkillLevel.BEGINNER)
                ));

        when(studentSkillRepository.findAllByUser_Email("test@gmail.com"))
                .thenReturn(List.of(
                        createStudentSkill(java, SkillLevel.INTERMEDIATE),
                        createStudentSkill(mysql, SkillLevel.BEGINNER)
                ));

        List<CareerRecommendationResponse> response =
                recommendationService.getRecommendations("test@gmail.com");

        assertNotNull(response);
        assertEquals(1, response.size());

        CareerRecommendationResponse recommendation = response.get(0);

        assertEquals("Java Backend Developer", recommendation.getCareerName());
        assertEquals(3, recommendation.getTotalRequiredSkills());
        assertEquals(2, recommendation.getMatchedSkillsCount());
        assertEquals(66.67, recommendation.getMatchPercentage(), 0.01);

        assertEquals(1, recommendation.getMissingSkills().size());
    }

    @Test
    void getRecommendationsShouldReturn100PercentWhenAllSkillsMatch() {

        when(careerRepository.findAll())
                .thenReturn(List.of(career));

        when(careerSkillRepository.findAllByCareer_Id(1L))
                .thenReturn(List.of(
                        createCareerSkill(java, SkillLevel.BEGINNER),
                        createCareerSkill(mysql, SkillLevel.BEGINNER)
                ));

        when(studentSkillRepository.findAllByUser_Email("test@gmail.com"))
                .thenReturn(List.of(
                        createStudentSkill(java, SkillLevel.ADVANCED),
                        createStudentSkill(mysql, SkillLevel.INTERMEDIATE)
                ));

        List<CareerRecommendationResponse> response =
                recommendationService.getRecommendations("test@gmail.com");

        assertEquals(1, response.size());
        assertEquals(100.0, response.get(0).getMatchPercentage(), 0.01);
        assertEquals(2, response.get(0).getMatchedSkillsCount());
    }

    @Test
    void getRecommendationsShouldReturnZeroWhenNoSkillsMatch() {

        when(careerRepository.findAll())
                .thenReturn(List.of(career));

        when(careerSkillRepository.findAllByCareer_Id(1L))
                .thenReturn(List.of(
                        createCareerSkill(java, SkillLevel.BEGINNER),
                        createCareerSkill(spring, SkillLevel.BEGINNER)
                ));

        when(studentSkillRepository.findAllByUser_Email("test@gmail.com"))
                .thenReturn(List.of(
                        createStudentSkill(mysql, SkillLevel.ADVANCED)
                ));

        List<CareerRecommendationResponse> response =
                recommendationService.getRecommendations("test@gmail.com");

        assertEquals(1, response.size());
        assertEquals(0.0, response.get(0).getMatchPercentage(), 0.01);
        assertEquals(0, response.get(0).getMatchedSkillsCount());
    }

    @Test
    void getRecommendationsShouldReturnEmptyListWhenNoCareersExist() {

        when(careerRepository.findAll())
                .thenReturn(List.of());

        when(studentSkillRepository.findAllByUser_Email("test@gmail.com"))
                .thenReturn(List.of());

        List<CareerRecommendationResponse> response =
                recommendationService.getRecommendations("test@gmail.com");

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }
}
