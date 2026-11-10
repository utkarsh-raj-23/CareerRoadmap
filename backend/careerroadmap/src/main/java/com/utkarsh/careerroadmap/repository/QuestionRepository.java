
package com.utkarsh.careerroadmap.repository;

import com.utkarsh.careerroadmap.entity.Question;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository
        extends JpaRepository<Question, Long> {

    List<Question> findAllByAssessment_IdOrderByOrderIndexAsc(
            Long assessmentId
    );

    long countByAssessment_Id(Long assessmentId);
}
