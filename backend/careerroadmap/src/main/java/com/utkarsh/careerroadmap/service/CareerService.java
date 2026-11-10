
package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.CreateCareerRequest;
import com.utkarsh.careerroadmap.dto.AddCareerSkillRequest;
import com.utkarsh.careerroadmap.dto.CareerResponse;
import com.utkarsh.careerroadmap.dto.CareerSkillResponse;

import com.utkarsh.careerroadmap.entity.Career;
import com.utkarsh.careerroadmap.entity.CareerSkill;
import com.utkarsh.careerroadmap.entity.Skill;

import com.utkarsh.careerroadmap.repository.CareerRepository;
import com.utkarsh.careerroadmap.repository.CareerSkillRepository;
import com.utkarsh.careerroadmap.repository.SkillRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class CareerService {

    private final CareerRepository careerRepository;
    private final CareerSkillRepository careerSkillRepository;
    private final SkillRepository skillRepository;

    public CareerService(
            CareerRepository careerRepository,
            CareerSkillRepository careerSkillRepository,
            SkillRepository skillRepository) {

        this.careerRepository = careerRepository;
        this.careerSkillRepository = careerSkillRepository;
        this.skillRepository = skillRepository;
    }

    // Create a new career
    @Transactional
    public CareerResponse createCareer(CreateCareerRequest request) {

        String careerName = request.getName().trim();

        if (careerRepository.existsByNameIgnoreCase(careerName)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Career already exists"
            );
        }

        Career career = new Career();

        career.setName(careerName);
        career.setCategory(request.getCategory());
        career.setDescription(request.getDescription());

        Career savedCareer = careerRepository.save(career);

        return convertToResponse(savedCareer);
    }

    // Get all careers
    @Transactional(readOnly = true)
    public List<CareerResponse> getAllCareers() {

        List<Career> careers = careerRepository.findAll();

        List<CareerResponse> responses = new ArrayList<>();

        for (Career career : careers) {
            responses.add(convertToResponse(career));
        }

        return responses;
    }

    // Get one career by ID
    @Transactional(readOnly = true)
    public CareerResponse getCareerById(Long careerId) {

        Career career = careerRepository.findById(careerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Career not found"
                ));

        return convertToResponse(career);
    }

    // Add a required skill to a career
    @Transactional
    public CareerSkillResponse addSkillToCareer(
            Long careerId,
            AddCareerSkillRequest request) {

        Career career = careerRepository.findById(careerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Career not found"
                ));

        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Skill not found"
                ));

        boolean alreadyExists =
                careerSkillRepository.existsByCareer_IdAndSkill_Id(
                        careerId,
                        request.getSkillId()
                );

        if (alreadyExists) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This skill is already assigned to the career"
            );
        }

        CareerSkill careerSkill = new CareerSkill();

        careerSkill.setCareer(career);
        careerSkill.setSkill(skill);
        careerSkill.setRequiredLevel(request.getRequiredLevel());
        careerSkill.setMandatory(request.isMandatory());

        CareerSkill saved = careerSkillRepository.save(careerSkill);

        return convertSkillToResponse(saved);
    }

    // Remove a required skill from a career
    @Transactional
    public void removeSkillFromCareer(
            Long careerId,
            Long skillId) {

        if (!careerRepository.existsById(careerId)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Career not found"
            );
        }

        CareerSkill careerSkill = careerSkillRepository
                .findByCareer_IdAndSkill_Id(careerId, skillId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Career skill not found"
                ));

        careerSkillRepository.delete(careerSkill);
    }

    // Convert Career entity into CareerResponse
    private CareerResponse convertToResponse(Career career) {

        List<CareerSkill> careerSkills =
                careerSkillRepository.findAllByCareer_Id(career.getId());

        List<CareerSkillResponse> skillResponses =
                new ArrayList<>();

        for (CareerSkill careerSkill : careerSkills) {
            skillResponses.add(convertSkillToResponse(careerSkill));
        }

        return new CareerResponse(
                career.getId(),
                career.getName(),
                career.getCategory(),
                career.getDescription(),
                skillResponses
        );
    }

    // Convert CareerSkill entity into CareerSkillResponse
    private CareerSkillResponse convertSkillToResponse(
            CareerSkill careerSkill) {

        return new CareerSkillResponse(
                careerSkill.getSkill().getId(),
                careerSkill.getSkill().getName(),
                careerSkill.getSkill().getCategory(),
                careerSkill.getRequiredLevel(),
                careerSkill.isMandatory()
        );
    }
}
