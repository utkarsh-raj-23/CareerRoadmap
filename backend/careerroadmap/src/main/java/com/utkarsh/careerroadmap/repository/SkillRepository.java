package com.utkarsh.careerroadmap.repository;

import com.utkarsh.careerroadmap.entity.LearningResource;
import com.utkarsh.careerroadmap.entity.Skill;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SkillRepository extends JpaRepository<Skill, Long> {

    Optional<Skill> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}