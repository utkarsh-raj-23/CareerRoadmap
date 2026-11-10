package com.utkarsh.careerroadmap.repository;

import com.utkarsh.careerroadmap.entity.StudentProfile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentProfileRepository
        extends JpaRepository<StudentProfile, Long> {

    Optional<StudentProfile> findByUser_Email(String email);

    boolean existsByUser_Email(String email);
}