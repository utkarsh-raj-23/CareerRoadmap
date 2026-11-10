package com.utkarsh.careerroadmap.repository;

import com.utkarsh.careerroadmap.entity.RoadmapItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoadmapItemRepository
        extends JpaRepository<RoadmapItem, Long> {

    List<RoadmapItem> findAllByRoadmap_IdOrderByOrderIndexAsc(
            Long roadmapId
    );

    Optional<RoadmapItem> findByIdAndRoadmap_Id(
            Long itemId,
            Long roadmapId
    );
}