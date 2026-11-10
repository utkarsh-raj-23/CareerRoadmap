
package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.AdminDashboardResponse;
import com.utkarsh.careerroadmap.repository.UserRepository;
import com.utkarsh.careerroadmap.repository.CareerRepository;
import com.utkarsh.careerroadmap.repository.SkillRepository;
import com.utkarsh.careerroadmap.repository.AssessmentRepository;
import com.utkarsh.careerroadmap.repository.LearningResourceRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CareerRepository careerRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private LearningResourceRepository learningResourceRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void getDashboardShouldReturnCorrectCounts() {

        when(userRepository.count()).thenReturn(100L);
        when(careerRepository.count()).thenReturn(15L);
        when(skillRepository.count()).thenReturn(50L);
        when(assessmentRepository.count()).thenReturn(10L);
        when(learningResourceRepository.count()).thenReturn(75L);

        AdminDashboardResponse response = adminService.getDashboard();

        assertNotNull(response);

        assertEquals(100L, response.getTotalUsers());
        assertEquals(15L, response.getTotalCareers());
        assertEquals(50L, response.getTotalSkills());
        assertEquals(10L, response.getTotalAssessments());
        assertEquals(75L, response.getTotalLearningResources());

        verify(userRepository).count();
        verify(careerRepository).count();
        verify(skillRepository).count();
        verify(assessmentRepository).count();
        verify(learningResourceRepository).count();
    }

    @Test
    void getDashboardShouldReturnZeroWhenDatabaseIsEmpty() {

        when(userRepository.count()).thenReturn(0L);
        when(careerRepository.count()).thenReturn(0L);
        when(skillRepository.count()).thenReturn(0L);
        when(assessmentRepository.count()).thenReturn(0L);
        when(learningResourceRepository.count()).thenReturn(0L);

        AdminDashboardResponse response = adminService.getDashboard();

        assertEquals(0L, response.getTotalUsers());
        assertEquals(0L, response.getTotalCareers());
        assertEquals(0L, response.getTotalSkills());
        assertEquals(0L, response.getTotalAssessments());
        assertEquals(0L, response.getTotalLearningResources());
    }
}
