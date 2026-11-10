package com.utkarsh.careerroadmap.repository;

import com.utkarsh.careerroadmap.entity.LearningResource;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LearningResourceRepository
        extends JpaRepository<LearningResource, Long> {

    List<LearningResource> findAllBySkill_Id(Long skillId);

    List<LearningResource> findAllBySkill_IdAndFreeTrue(Long skillId);
}