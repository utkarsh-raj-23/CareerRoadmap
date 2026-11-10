package com.utkarsh.careerroadmap.repository;

import com.utkarsh.careerroadmap.entity.Roadmap;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoadmapRepository
        extends JpaRepository<Roadmap, Long> {

    List<Roadmap> findAllByUser_EmailOrderByCreatedAtDesc(
            String email
    );

    Optional<Roadmap> findByIdAndUser_Email(
            Long id,
            String email
    );
}