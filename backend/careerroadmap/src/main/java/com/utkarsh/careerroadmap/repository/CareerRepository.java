
package com.utkarsh.careerroadmap.repository;

import com.utkarsh.careerroadmap.entity.Career;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CareerRepository
        extends JpaRepository<Career, Long> {

    Optional<Career> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);
}
