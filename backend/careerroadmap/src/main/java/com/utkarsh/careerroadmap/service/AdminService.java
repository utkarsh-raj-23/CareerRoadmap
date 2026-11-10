package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.AdminDashboardResponse;

import com.utkarsh.careerroadmap.repository.UserRepository;
import com.utkarsh.careerroadmap.repository.CareerRepository;
import com.utkarsh.careerroadmap.repository.SkillRepository;
import com.utkarsh.careerroadmap.repository.AssessmentRepository;
import com.utkarsh.careerroadmap.repository.LearningResourceRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final CareerRepository careerRepository;
    private final SkillRepository skillRepository;
    private final AssessmentRepository assessmentRepository;
    private final LearningResourceRepository resourceRepository;

    public AdminService(
            UserRepository userRepository,
            CareerRepository careerRepository,
            SkillRepository skillRepository,
            AssessmentRepository assessmentRepository,
            LearningResourceRepository resourceRepository) {

        this.userRepository = userRepository;
        this.careerRepository = careerRepository;
        this.skillRepository = skillRepository;
        this.assessmentRepository = assessmentRepository;
        this.resourceRepository = resourceRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {

        return new AdminDashboardResponse(
                userRepository.count(),
                careerRepository.count(),
                skillRepository.count(),
                assessmentRepository.count(),
                resourceRepository.count()
        );
    }
}