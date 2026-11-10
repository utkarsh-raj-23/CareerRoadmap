
package com.utkarsh.careerroadmap.repository;

import com.utkarsh.careerroadmap.entity.QuestionOption;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionOptionRepository
        extends JpaRepository<QuestionOption, Long> {

    List<QuestionOption> findAllByQuestion_Id(Long questionId);
}
