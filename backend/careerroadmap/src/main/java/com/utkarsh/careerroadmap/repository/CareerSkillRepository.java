
package com.utkarsh.careerroadmap.repository;

import com.utkarsh.careerroadmap.entity.CareerSkill;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CareerSkillRepository
        extends JpaRepository<CareerSkill, Long> {

    List<CareerSkill> findAllByCareer_Id(Long careerId);

    Optional<CareerSkill> findByCareer_IdAndSkill_Id(
            Long careerId,
            Long skillId
    );

    boolean existsByCareer_IdAndSkill_Id(
            Long careerId,
            Long skillId
    );

    void deleteByCareer_IdAndSkill_Id(
            Long careerId,
            Long skillId
    );
}
