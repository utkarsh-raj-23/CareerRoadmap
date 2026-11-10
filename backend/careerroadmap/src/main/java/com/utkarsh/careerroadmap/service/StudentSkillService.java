package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.AddSkillRequest;
import com.utkarsh.careerroadmap.dto.UpdateSkillRequest;
import com.utkarsh.careerroadmap.dto.StudentSkillResponse;
import com.utkarsh.careerroadmap.dto.SkillsAnalysisResponse;

import com.utkarsh.careerroadmap.entity.Skill;
import com.utkarsh.careerroadmap.entity.StudentSkill;
import com.utkarsh.careerroadmap.entity.User;

import com.utkarsh.careerroadmap.repository.SkillRepository;
import com.utkarsh.careerroadmap.repository.StudentSkillRepository;
import com.utkarsh.careerroadmap.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class StudentSkillService {

    private final SkillRepository skillRepository;
    private final StudentSkillRepository studentSkillRepository;
    private final UserRepository userRepository;

    public StudentSkillService(
            SkillRepository skillRepository,
            StudentSkillRepository studentSkillRepository,
            UserRepository userRepository) {

        this.skillRepository = skillRepository;
        this.studentSkillRepository = studentSkillRepository;
        this.userRepository = userRepository;
    }

    // Save skills detected by Python.
    @Transactional
    public int saveExtractedSkills(
            String email,
            SkillsAnalysisResponse skillsAnalysis) {

        User user = getUser(email);

        if (skillsAnalysis == null ||
                skillsAnalysis.getCategorized_skills() == null) {
            return 0;
        }

        int savedCount = 0;

        for (Map.Entry<String, List<String>> entry :
                skillsAnalysis.getCategorized_skills().entrySet()) {

            String category = entry.getKey();

            for (String skillName : entry.getValue()) {

                if (skillName == null || skillName.isBlank()) {
                    continue;
                }

                String cleanSkillName = skillName.trim();

                Skill skill = skillRepository
                        .findByNameIgnoreCase(cleanSkillName)
                        .orElseGet(() -> {
                            Skill newSkill = new Skill();
                            newSkill.setName(cleanSkillName);
                            newSkill.setCategory(category);
                            return skillRepository.save(newSkill);
                        });

                boolean alreadyExists =
                        studentSkillRepository
                                .existsByUser_EmailAndSkill_NameIgnoreCase(
                                        email, cleanSkillName);

                if (!alreadyExists) {
                    StudentSkill studentSkill = new StudentSkill();
                    studentSkill.setUser(user);
                    studentSkill.setSkill(skill);

                    studentSkillRepository.save(studentSkill);
                    savedCount++;
                }
            }
        }

        return savedCount;
    }

    // Get all skills belonging to a student.
    @Transactional(readOnly = true)
    public List<StudentSkillResponse> getMySkills(String email) {

        List<StudentSkill> studentSkills =
                studentSkillRepository.findAllByUser_Email(email);

        List<StudentSkillResponse> responses = new ArrayList<>();

        for (StudentSkill studentSkill : studentSkills) {
            responses.add(convertToResponse(studentSkill));
        }

        return responses;
    }

    // Manually add a skill.
    @Transactional
    public StudentSkillResponse addSkill(
            String email,
            AddSkillRequest request) {

        User user = getUser(email);

        String skillName = request.getName().trim();

        Skill skill = skillRepository
                .findByNameIgnoreCase(skillName)
                .orElseGet(() -> {
                    Skill newSkill = new Skill();
                    newSkill.setName(skillName);
                    newSkill.setCategory(request.getCategory());
                    return skillRepository.save(newSkill);
                });

        if (studentSkillRepository
                .existsByUser_EmailAndSkill_NameIgnoreCase(
                        email, skillName)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already have this skill"
            );
        }

        StudentSkill studentSkill = new StudentSkill();

        studentSkill.setUser(user);
        studentSkill.setSkill(skill);
        studentSkill.setProficiencyLevel(request.getProficiencyLevel());
        studentSkill.setYearsOfExperience(request.getYearsOfExperience());

        StudentSkill saved =
                studentSkillRepository.save(studentSkill);

        return convertToResponse(saved);
    }

    // Update proficiency and experience.
    @Transactional
    public StudentSkillResponse updateSkill(
            String email,
            Long skillId,
            UpdateSkillRequest request) {

        StudentSkill studentSkill = studentSkillRepository
                .findById(skillId)
                .filter(s -> s.getUser().getEmail().equals(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student skill not found"
                ));

        studentSkill.setProficiencyLevel(
                request.getProficiencyLevel());

        studentSkill.setYearsOfExperience(
                request.getYearsOfExperience());

        StudentSkill updated =
                studentSkillRepository.save(studentSkill);

        return convertToResponse(updated);
    }

    // Delete a skill from a student's profile.
    @Transactional
    public void deleteSkill(String email, Long skillId) {

        StudentSkill studentSkill = studentSkillRepository
                .findById(skillId)
                .filter(s -> s.getUser().getEmail().equals(email))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student skill not found"
                ));

        studentSkillRepository.delete(studentSkill);
    }

    // Find user by email.
    private User getUser(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    // Convert entity into response DTO.
    private StudentSkillResponse convertToResponse(
            StudentSkill studentSkill) {

        return new StudentSkillResponse(
                studentSkill.getId(),
                studentSkill.getSkill().getName(),
                studentSkill.getSkill().getCategory(),
                studentSkill.getProficiencyLevel(),
                studentSkill.getYearsOfExperience()
        );
    }
}