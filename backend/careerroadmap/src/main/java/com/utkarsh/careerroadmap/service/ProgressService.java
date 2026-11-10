package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.ProgressSummaryResponse;

import com.utkarsh.careerroadmap.entity.Roadmap;
import com.utkarsh.careerroadmap.entity.RoadmapItem;

import com.utkarsh.careerroadmap.enums.RoadmapItemStatus;

import com.utkarsh.careerroadmap.repository.RoadmapRepository;
import com.utkarsh.careerroadmap.repository.RoadmapItemRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProgressService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapItemRepository roadmapItemRepository;

    public ProgressService(
            RoadmapRepository roadmapRepository,
            RoadmapItemRepository roadmapItemRepository) {

        this.roadmapRepository = roadmapRepository;
        this.roadmapItemRepository = roadmapItemRepository;
    }

    // Overall progress across all the student's roadmaps.
    @Transactional(readOnly = true)
    public ProgressSummaryResponse getOverallProgress(String email) {

        List<Roadmap> roadmaps =
                roadmapRepository.findAllByUser_EmailOrderByCreatedAtDesc(
                        email
                );

        return calculateProgress(null, "All Roadmaps", roadmaps);
    }

    // Progress for one particular roadmap.
    @Transactional(readOnly = true)
    public ProgressSummaryResponse getRoadmapProgress(
            Long roadmapId,
            String email) {

        Roadmap roadmap = roadmapRepository
                .findByIdAndUser_Email(roadmapId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Roadmap not found"
                ));

        List<Roadmap> roadmaps = new ArrayList<>();
        roadmaps.add(roadmap);

        return calculateProgress(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmaps
        );
    }

    private ProgressSummaryResponse calculateProgress(
            Long roadmapId,
            String title,
            List<Roadmap> roadmaps) {

        int totalTasks = 0;
        int completedTasks = 0;
        int inProgressTasks = 0;
        int pendingTasks = 0;

        int totalEstimatedHours = 0;
        int completedEstimatedHours = 0;

        for (Roadmap roadmap : roadmaps) {

            List<RoadmapItem> items =
                    roadmapItemRepository
                            .findAllByRoadmap_IdOrderByOrderIndexAsc(
                                    roadmap.getId()
                            );

            for (RoadmapItem item : items) {

                totalTasks++;

                int hours = item.getEstimatedHours() == null
                        ? 0
                        : item.getEstimatedHours();

                totalEstimatedHours += hours;

                if (item.getStatus() == RoadmapItemStatus.COMPLETED) {

                    completedTasks++;
                    completedEstimatedHours += hours;

                } else if (
                        item.getStatus() == RoadmapItemStatus.IN_PROGRESS) {

                    inProgressTasks++;

                } else {

                    pendingTasks++;
                }
            }
        }

        double completionPercentage = 0.0;

        if (totalTasks > 0) {
            completionPercentage =
                    (completedTasks * 100.0) / totalTasks;

            completionPercentage =
                    Math.round(completionPercentage * 100.0) / 100.0;
        }

        return new ProgressSummaryResponse(
                roadmapId,
                title,
                totalTasks,
                completedTasks,
                inProgressTasks,
                pendingTasks,
                totalEstimatedHours,
                completedEstimatedHours,
                completionPercentage
        );
    }
}