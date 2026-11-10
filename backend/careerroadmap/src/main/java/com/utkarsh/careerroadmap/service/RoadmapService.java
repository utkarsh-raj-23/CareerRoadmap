package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.*;

import com.utkarsh.careerroadmap.entity.*;

import com.utkarsh.careerroadmap.enums.RoadmapItemStatus;
import com.utkarsh.careerroadmap.enums.SkillLevel;

import com.utkarsh.careerroadmap.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapItemRepository roadmapItemRepository;

    private final CareerRepository careerRepository;
    private final SkillRepository skillRepository;
    private final UserRepository userRepository;

    private final CareerRecommendationService recommendationService;

    public RoadmapService(
            RoadmapRepository roadmapRepository,
            RoadmapItemRepository roadmapItemRepository,
            CareerRepository careerRepository,
            SkillRepository skillRepository,
            UserRepository userRepository,
            CareerRecommendationService recommendationService) {

        this.roadmapRepository = roadmapRepository;
        this.roadmapItemRepository = roadmapItemRepository;
        this.careerRepository = careerRepository;
        this.skillRepository = skillRepository;
        this.userRepository = userRepository;
        this.recommendationService = recommendationService;
    }

    // Generate a roadmap for a selected career.
    @Transactional
    public RoadmapResponse createRoadmap(
            String email,
            CreateRoadmapRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        Career career = careerRepository.findById(
                request.getCareerId()
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Career not found"
        ));

        CareerRecommendationResponse recommendation =
                recommendationService.getSkillGap(
                        career.getId(),
                        email
                );

        List<CareerSkillGapResponse> gaps =
                recommendation.getProficiencyGaps();

        if (gaps == null || gaps.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "You have no identified skill gaps for this career"
            );
        }

        // Put mandatory skills before optional skills.
        gaps.sort(
                Comparator.comparing(
                                CareerSkillGapResponse::isMandatory
                        ).reversed()
                        .thenComparing(
                                CareerSkillGapResponse::getSkillName
                        )
        );

        Roadmap roadmap = new Roadmap();

        roadmap.setUser(user);
        roadmap.setCareer(career);
        roadmap.setTitle(
                career.getName() + " Learning Roadmap"
        );

        Roadmap savedRoadmap = roadmapRepository.save(roadmap);

        int order = 1;

        for (CareerSkillGapResponse gap : gaps) {

            Skill skill = skillRepository.findById(
                    gap.getSkillId()
            ).orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Skill not found"
            ));

            SkillLevel currentLevel = gap.getCurrentLevel();

            // If the student does not have the skill,
            // use BEGINNER as the starting level for planning.
            if (currentLevel == null) {
                currentLevel = SkillLevel.BEGINNER;
            }

            SkillLevel targetLevel = gap.getRequiredLevel();

            int levelDifference = Math.max(
                    1,
                    targetLevel.ordinal() - currentLevel.ordinal()
            );

            // Initial planning estimate: 20 hours per level step.
            int estimatedHours = levelDifference * 20;

            RoadmapItem item = new RoadmapItem();

            item.setRoadmap(savedRoadmap);
            item.setSkill(skill);
            item.setOrderIndex(order++);
            item.setCurrentLevel(gap.getCurrentLevel());
            item.setTargetLevel(targetLevel);
            item.setEstimatedHours(estimatedHours);
            item.setStatus(RoadmapItemStatus.PENDING);

            roadmapItemRepository.save(item);
        }

        return convertToResponse(savedRoadmap);
    }

    // Get all roadmaps belonging to the logged-in student.
    @Transactional(readOnly = true)
    public List<RoadmapResponse> getMyRoadmaps(String email) {

        List<Roadmap> roadmaps =
                roadmapRepository.findAllByUser_EmailOrderByCreatedAtDesc(
                        email
                );

        List<RoadmapResponse> responses = new ArrayList<>();

        for (Roadmap roadmap : roadmaps) {
            responses.add(convertToResponse(roadmap));
        }

        return responses;
    }

    // Get one particular roadmap.
    @Transactional(readOnly = true)
    public RoadmapResponse getRoadmap(
            Long roadmapId,
            String email) {

        Roadmap roadmap = getOwnedRoadmap(roadmapId, email);

        return convertToResponse(roadmap);
    }

    // Update the status of a roadmap task.
    @Transactional
    public RoadmapItemResponse updateItemStatus(
            Long roadmapId,
            Long itemId,
            String email,
            UpdateRoadmapItemStatusRequest request) {

        Roadmap roadmap = getOwnedRoadmap(roadmapId, email);

        RoadmapItem item = roadmapItemRepository
                .findByIdAndRoadmap_Id(itemId, roadmap.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Roadmap item not found"
                ));

        RoadmapItemStatus newStatus = request.getStatus();

        item.setStatus(newStatus);

        if (newStatus == RoadmapItemStatus.COMPLETED) {
            item.setCompletedAt(LocalDateTime.now());
        } else {
            item.setCompletedAt(null);
        }

        RoadmapItem saved = roadmapItemRepository.save(item);

        return convertItemToResponse(saved);
    }

    private Roadmap getOwnedRoadmap(
            Long roadmapId,
            String email) {

        return roadmapRepository.findByIdAndUser_Email(
                roadmapId,
                email
        ).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Roadmap not found"
        ));
    }

    private RoadmapResponse convertToResponse(Roadmap roadmap) {

        List<RoadmapItem> items =
                roadmapItemRepository
                        .findAllByRoadmap_IdOrderByOrderIndexAsc(
                                roadmap.getId()
                        );

        List<RoadmapItemResponse> itemResponses =
                new ArrayList<>();

        for (RoadmapItem item : items) {
            itemResponses.add(convertItemToResponse(item));
        }

        return new RoadmapResponse(
                roadmap.getId(),
                roadmap.getTitle(),
                roadmap.getCareer().getId(),
                roadmap.getCareer().getName(),
                roadmap.getCreatedAt(),
                itemResponses
        );
    }

    private RoadmapItemResponse convertItemToResponse(
            RoadmapItem item) {

        return new RoadmapItemResponse(
                item.getId(),
                item.getSkill().getName(),
                item.getOrderIndex(),
                item.getCurrentLevel(),
                item.getTargetLevel(),
                item.getEstimatedHours(),
                item.getStatus(),
                item.getCompletedAt()
        );
    }
}