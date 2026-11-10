
package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.StudentSkillResponse;
import com.utkarsh.careerroadmap.entity.Skill;
import com.utkarsh.careerroadmap.entity.StudentSkill;
import com.utkarsh.careerroadmap.entity.User;
import com.utkarsh.careerroadmap.enums.SkillLevel;
import com.utkarsh.careerroadmap.repository.SkillRepository;
import com.utkarsh.careerroadmap.repository.StudentSkillRepository;
import com.utkarsh.careerroadmap.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentSkillServiceTest {

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private StudentSkillRepository studentSkillRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StudentSkillService studentSkillService;

    private User user;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setFullName("Test Student");
        user.setEmail("test@gmail.com");
    }

    @Test
    void getMySkillsShouldReturnStudentSkills() {

        Skill java = new Skill();
        java.setId(1L);
        java.setName("Java");
        java.setCategory("Programming");

        Skill spring = new Skill();
        spring.setId(2L);
        spring.setName("Spring Boot");
        spring.setCategory("Backend");

        StudentSkill studentSkill1 = new StudentSkill();
        studentSkill1.setId(1L);
        studentSkill1.setUser(user);
        studentSkill1.setSkill(java);
        studentSkill1.setProficiencyLevel(SkillLevel.INTERMEDIATE);
        studentSkill1.setYearsOfExperience((int) 1.0);

        StudentSkill studentSkill2 = new StudentSkill();
        studentSkill2.setId(2L);
        studentSkill2.setUser(user);
        studentSkill2.setSkill(spring);
        studentSkill2.setProficiencyLevel(SkillLevel.BEGINNER);
        studentSkill2.setYearsOfExperience((int) 0.5);

        List<StudentSkill> skills = new ArrayList<>();
        skills.add(studentSkill1);
        skills.add(studentSkill2);

        when(studentSkillRepository.findAllByUser_Email("test@gmail.com"))
                .thenReturn(skills);

        List<StudentSkillResponse> response =
                studentSkillService.getMySkills("test@gmail.com");

        assertNotNull(response);
        assertEquals(2, response.size());

        assertEquals("Java", response.get(0).getSkillName());
        assertEquals("Programming", response.get(0).getCategory());
        assertEquals(
                SkillLevel.INTERMEDIATE,
                response.get(0).getProficiencyLevel()
        );

        assertEquals("Spring Boot", response.get(1).getSkillName());
        assertEquals("Backend", response.get(1).getCategory());
        assertEquals(
                SkillLevel.BEGINNER,
                response.get(1).getProficiencyLevel()
        );

        verify(studentSkillRepository)
                .findAllByUser_Email("test@gmail.com");
    }

    @Test
    void getMySkillsShouldReturnEmptyListWhenNoSkillsExist() {

        when(studentSkillRepository.findAllByUser_Email("test@gmail.com"))
                .thenReturn(new ArrayList<>());

        List<StudentSkillResponse> response =
                studentSkillService.getMySkills("test@gmail.com");

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }
}
