
package com.utkarsh.careerroadmap.repository;

import com.utkarsh.careerroadmap.entity.StudentAssessment;
import com.utkarsh.careerroadmap.enums.AssessmentStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentAssessmentRepository
        extends JpaRepository<StudentAssessment, Long> {

    List<StudentAssessment> findAllByUser_EmailOrderByStartedAtDesc(
            String email
    );

    Optional<StudentAssessment> findByIdAndUser_Email(
            Long id,
            String email
    );

    boolean existsByUser_EmailAndAssessment_IdAndStatus(
            String email,
            Long assessmentId,
            AssessmentStatus status
    );
}
