package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.CareerRecommendationResponse;
import com.utkarsh.careerroadmap.dto.CareerSkillGapResponse;
import com.utkarsh.careerroadmap.dto.SemanticSkillAnalysisRequest;
import com.utkarsh.careerroadmap.dto.SemanticSkillAnalysisResponse;

import com.utkarsh.careerroadmap.entity.Career;
import com.utkarsh.careerroadmap.entity.CareerSkill;
import com.utkarsh.careerroadmap.entity.StudentSkill;

import com.utkarsh.careerroadmap.enums.SkillLevel;

import com.utkarsh.careerroadmap.repository.CareerRepository;
import com.utkarsh.careerroadmap.repository.CareerSkillRepository;
import com.utkarsh.careerroadmap.repository.StudentSkillRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class CareerRecommendationService {

    private final CareerRepository careerRepository;
    private final CareerSkillRepository careerSkillRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final ResumeAnalysisService resumeAnalysisService;

    public CareerRecommendationService(
            CareerRepository careerRepository,
            CareerSkillRepository careerSkillRepository,
            StudentSkillRepository studentSkillRepository,
            ResumeAnalysisService resumeAnalysisService) {

        this.careerRepository = careerRepository;
        this.careerSkillRepository = careerSkillRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.resumeAnalysisService = resumeAnalysisService;
    }

    @Transactional(readOnly = true)
    public List<CareerRecommendationResponse> getRecommendations(
            String email) {

        List<Career> careers = careerRepository.findAll();

        List<StudentSkill> studentSkills =
                studentSkillRepository.findAllByUser_Email(email);

        // Store the student's skills for quick lookup.
        Map<String, StudentSkill> studentSkillMap = new HashMap<>();

        for (StudentSkill studentSkill : studentSkills) {

            String skillName = studentSkill.getSkill()
                    .getName()
                    .toLowerCase();

            studentSkillMap.put(skillName, studentSkill);
        }

        List<CareerRecommendationResponse> recommendations =
                new ArrayList<>();

        for (Career career : careers) {

            List<CareerSkill> requiredSkills =
                    careerSkillRepository.findAllByCareer_Id(
                            career.getId()
                    );

            // Ignore careers without defined required skills.
            if (requiredSkills.isEmpty()) {
                continue;
            }

            List<String> matchedSkills = new ArrayList<>();
            List<String> missingSkills = new ArrayList<>();

            List<CareerSkillGapResponse> proficiencyGaps =
                    new ArrayList<>();

            for (CareerSkill careerSkill : requiredSkills) {

                String skillName = careerSkill.getSkill()
                        .getName();

                StudentSkill studentSkill =
                        studentSkillMap.get(
                                skillName.toLowerCase()
                        );

                if (studentSkill == null) {

                    missingSkills.add(skillName);

                    proficiencyGaps.add(
                            new CareerSkillGapResponse(
                                    careerSkill.getSkill().getId(),
                                    skillName,
                                    null,
                                    careerSkill.getRequiredLevel(),
                                    careerSkill.isMandatory()
                            )
                    );

                } else {

                    matchedSkills.add(skillName);

                    SkillLevel currentLevel =
                            studentSkill.getProficiencyLevel();

                    SkillLevel requiredLevel =
                            careerSkill.getRequiredLevel();

                    // A null level means the student's
                    // proficiency has not yet been assessed.
                    if (currentLevel == null ||
                            currentLevel.ordinal() < requiredLevel.ordinal()) {

                        proficiencyGaps.add(
                                new CareerSkillGapResponse(
                                        careerSkill.getSkill().getId(),
                                        skillName,
                                        currentLevel,
                                        requiredLevel,
                                        careerSkill.isMandatory()
                                )
                        );
                    }
                }
            }

            int totalRequiredSkills = requiredSkills.size();
            int matchedCount = matchedSkills.size();

            double matchPercentage =
                    (matchedCount * 100.0) / totalRequiredSkills;

            matchPercentage =
                    Math.round(matchPercentage * 100.0) / 100.0;

            CareerRecommendationResponse response =
                    new CareerRecommendationResponse(
                            career.getId(),
                            career.getName(),
                            career.getCategory(),
                            career.getDescription(),
                            matchPercentage,
                            totalRequiredSkills,
                            matchedCount,
                            matchedSkills,
                            missingSkills,
                            proficiencyGaps
                    );

            recommendations.add(response);
        }

        // Highest match percentage appears first.
        recommendations.sort(
                Comparator.comparingDouble(
                        CareerRecommendationResponse::getMatchPercentage
                ).reversed()
        );

        return recommendations;
    }

    @Transactional(readOnly = true)
    public CareerRecommendationResponse getSkillGap(
            Long careerId,
            String email) {

        List<CareerRecommendationResponse> recommendations =
                getRecommendations(email);

        return recommendations.stream()
                .filter(r -> r.getCareerId().equals(careerId))
                .findFirst()
                .orElseThrow(() ->
                        new org.springframework.web.server.ResponseStatusException(
                                org.springframework.http.HttpStatus.NOT_FOUND,
                                "Career not found or no required skills defined"
                        )
                );
    }

    /**
     * FEATURE 3: Deep Semantic Skill Analysis & Gap Breakdown for a selected career.
     */
    @Transactional(readOnly = true)
    public SemanticSkillAnalysisResponse getSemanticSkillAnalysis(
            Long careerId,
            String email) {

        Career career = careerRepository.findById(careerId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Career not found"
                        )
                );

        List<StudentSkill> studentSkills =
                studentSkillRepository.findAllByUser_Email(email);

        List<String> skillNames = studentSkills.stream()
                .map(ss -> ss.getSkill().getName())
                .toList();

        SemanticSkillAnalysisRequest request = new SemanticSkillAnalysisRequest(
                null,
                skillNames,
                career.getName()
        );

        return resumeAnalysisService.analyzeSkills(request);
    }
}