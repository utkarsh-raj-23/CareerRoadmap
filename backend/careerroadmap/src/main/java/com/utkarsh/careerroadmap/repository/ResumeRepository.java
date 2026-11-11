
package com.utkarsh.careerroadmap.repository;

import com.utkarsh.careerroadmap.entity.Resume;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository
        extends JpaRepository<Resume, Long> {

    List<Resume> findAllByUser_EmailOrderByUploadedAtDesc(
            String email
    );

    Optional<Resume> findByIdAndUser_Email(
            Long id,
            String email
    );
    Optional<Resume> findFirstByUser_EmailOrderByUploadedAtDesc(
            String email
    );

    boolean existsByStoredFileName(String storedFileName);
}
