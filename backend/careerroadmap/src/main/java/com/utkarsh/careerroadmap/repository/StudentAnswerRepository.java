
package com.utkarsh.careerroadmap.repository;

import com.utkarsh.careerroadmap.entity.StudentAnswer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentAnswerRepository
        extends JpaRepository<StudentAnswer, Long> {

    List<StudentAnswer> findAllByStudentAssessment_Id(Long assessmentId);

    Optional<StudentAnswer> findByStudentAssessment_IdAndQuestion_Id(
            Long studentAssessmentId,
            Long questionId
    );

    boolean existsByStudentAssessment_IdAndQuestion_Id(
            Long studentAssessmentId,
            Long questionId
    );
}
