package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.CreateLearningResourceRequest;
import com.utkarsh.careerroadmap.dto.LearningResourceResponse;

import com.utkarsh.careerroadmap.entity.LearningResource;
import com.utkarsh.careerroadmap.entity.Skill;

import com.utkarsh.careerroadmap.repository.LearningResourceRepository;
import com.utkarsh.careerroadmap.repository.SkillRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class LearningResourceService {

    private final LearningResourceRepository resourceRepository;
    private final SkillRepository skillRepository;

    public LearningResourceService(
            LearningResourceRepository resourceRepository,
            SkillRepository skillRepository) {

        this.resourceRepository = resourceRepository;
        this.skillRepository = skillRepository;
    }

    @Transactional
    public LearningResourceResponse createResource(
            CreateLearningResourceRequest request) {

        Skill skill = skillRepository.findById(
                request.getSkillId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Skill not found"
        ));

        LearningResource resource = new LearningResource();

        resource.setSkill(skill);
        resource.setTitle(request.getTitle().trim());
        resource.setDescription(request.getDescription());
        resource.setUrl(request.getUrl().trim());
        resource.setProvider(request.getProvider());
        resource.setResourceType(request.getResourceType());
        resource.setFree(request.isFree());

        LearningResource saved =
                resourceRepository.save(resource);

        return convertToResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<LearningResourceResponse> getAllResources() {

        List<LearningResource> resources =
                resourceRepository.findAll();

        List<LearningResourceResponse> responses =
                new ArrayList<>();

        for (LearningResource resource : resources) {
            responses.add(convertToResponse(resource));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<LearningResourceResponse> getResourcesBySkill(
            String title,
            boolean freeOnly) {

        if ( resourceRepository.findByTitleContainingIgnoreCase(title)==null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Resource not found"
            );
        }

        List<LearningResource> resources;

        if (freeOnly) {
            resources = resourceRepository
                    .findByTitleContainingIgnoreCaseAndFreeTrue( title);
        } else {
            resources = resourceRepository
                    .findByTitleContainingIgnoreCaseAndFreeTrue( title);
        }

        List<LearningResourceResponse> responses =
                new ArrayList<>();

        for (LearningResource resource : resources) {
            responses.add(convertToResponse(resource));
        }

        return responses;
    }

    @Transactional(readOnly = true)
    public List<LearningResourceResponse> searchByTitle(
            String title,
            boolean freeOnly) {

        List<LearningResource> resources;

        if (freeOnly) {
            resources = resourceRepository
                    .findByTitleContainingIgnoreCaseAndFreeTrue(title);
        } else {
            resources = resourceRepository
                    .findByTitleContainingIgnoreCase(title);
        }

        List<LearningResourceResponse> responses =
                new ArrayList<>();

        for (LearningResource resource : resources) {
            responses.add(convertToResponse(resource));
        }

        return responses;
    }

    private LearningResourceResponse convertToResponse(
            LearningResource resource) {

        return new LearningResourceResponse(
                resource.getId(),
                resource.getSkill().getId(),
                resource.getSkill().getName(),
                resource.getTitle(),
                resource.getDescription(),
                resource.getUrl(),
                resource.getProvider(),
                resource.getResourceType(),
                resource.isFree()
        );
    }
}