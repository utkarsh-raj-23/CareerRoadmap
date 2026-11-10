package com.utkarsh.careerroadmap.repository;

import com.utkarsh.careerroadmap.entity.StudentSkill;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentSkillRepository
        extends JpaRepository<StudentSkill, Long> {

    List<StudentSkill> findAllByUser_Email(String email);

    Optional<StudentSkill> findByUser_EmailAndSkill_NameIgnoreCase(
            String email,
            String skillName
    );

    boolean existsByUser_EmailAndSkill_NameIgnoreCase(
            String email,
            String skillName
    );

    void deleteByUser_EmailAndSkill_Id(
            String email,
            Long skillId
    );
}