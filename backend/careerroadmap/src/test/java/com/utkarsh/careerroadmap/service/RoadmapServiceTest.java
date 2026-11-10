
package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.*;
import com.utkarsh.careerroadmap.entity.*;
import com.utkarsh.careerroadmap.enums.RoadmapItemStatus;
import com.utkarsh.careerroadmap.enums.SkillLevel;
import com.utkarsh.careerroadmap.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoadmapServiceTest {

    @Mock
    private RoadmapRepository roadmapRepository;

    @Mock
    private RoadmapItemRepository roadmapItemRepository;

    @Mock
    private CareerRepository careerRepository;

    @Mock
    private SkillRepository skillRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CareerRecommendationService recommendationService;

    @InjectMocks
    private RoadmapService roadmapService;

    private User user;
    private Career career;
    private Roadmap roadmap;
    private Skill skill;

    private final String email = "student@example.com";

    @BeforeEach
    void setUp() {

        user = new User();
        user.setEmail(email);

        career = new Career();
        career.setId(1L);
        career.setName("Java Developer");

        skill = new Skill();
        skill.setId(10L);
        skill.setName("Java");

        roadmap = new Roadmap();
        roadmap.setId(100L);
        roadmap.setUser(user);
        roadmap.setCareer(career);
        roadmap.setTitle("Java Developer Learning Roadmap");
    }

    // 1. Create roadmap successfully

    @Test
    void createRoadmapShouldGenerateRoadmapSuccessfully() {

        CreateRoadmapRequest request =
                mock(CreateRoadmapRequest.class);

        when(request.getCareerId()).thenReturn(1L);

        CareerSkillGapResponse gap =
                mock(CareerSkillGapResponse.class);

        when(gap.getSkillId()).thenReturn(10L);
        when(gap.getCurrentLevel()).thenReturn(SkillLevel.BEGINNER);
        when(gap.getRequiredLevel()).thenReturn(SkillLevel.INTERMEDIATE);

        CareerRecommendationResponse recommendation =
                mock(CareerRecommendationResponse.class);

        when(recommendation.getProficiencyGaps())
                .thenReturn(new ArrayList<>(List.of(gap)));

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(careerRepository.findById(1L))
                .thenReturn(Optional.of(career));

        when(recommendationService.getSkillGap(1L, email))
                .thenReturn(recommendation);

        when(roadmapRepository.save(any(Roadmap.class)))
                .thenReturn(roadmap);

        when(skillRepository.findById(10L))
                .thenReturn(Optional.of(skill));

        when(roadmapItemRepository
                .findAllByRoadmap_IdOrderByOrderIndexAsc(100L))
                .thenReturn(List.of());

        RoadmapResponse response =
                roadmapService.createRoadmap(email, request);

        assertNotNull(response);
        assertEquals("Java Developer Learning Roadmap",
                response.getTitle());

        verify(roadmapRepository).save(any(Roadmap.class));
        verify(skillRepository).findById(10L);
        verify(roadmapItemRepository).save(any(RoadmapItem.class));
    }

    // 2. Create roadmap should fail when user does not exist

    @Test
    void createRoadmapShouldFailWhenUserNotFound() {

        CreateRoadmapRequest request =
                mock(CreateRoadmapRequest.class);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> roadmapService.createRoadmap(email, request)
        );

        verify(roadmapRepository, never()).save(any());
    }

    // 3. Create roadmap should fail when career does not exist

    @Test
    void createRoadmapShouldFailWhenCareerNotFound() {

        CreateRoadmapRequest request =
                mock(CreateRoadmapRequest.class);

        when(request.getCareerId()).thenReturn(1L);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(careerRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> roadmapService.createRoadmap(email, request)
        );

        verify(roadmapRepository, never()).save(any());
    }

    // 4. Create roadmap should fail when there are no skill gaps

    @Test
    void createRoadmapShouldFailWhenNoSkillGapsExist() {

        CreateRoadmapRequest request =
                mock(CreateRoadmapRequest.class);

        when(request.getCareerId()).thenReturn(1L);

        CareerRecommendationResponse recommendation =
                mock(CareerRecommendationResponse.class);

        when(recommendation.getProficiencyGaps())
                .thenReturn(new ArrayList<>());

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(careerRepository.findById(1L))
                .thenReturn(Optional.of(career));

        when(recommendationService.getSkillGap(1L, email))
                .thenReturn(recommendation);

        assertThrows(
                ResponseStatusException.class,
                () -> roadmapService.createRoadmap(email, request)
        );

        verify(roadmapRepository, never()).save(any());
    }

    // 5. Create roadmap should fail when a skill does not exist

    @Test
    void createRoadmapShouldFailWhenSkillNotFound() {
        CreateRoadmapRequest request = mock(CreateRoadmapRequest.class);
        when(request.getCareerId()).thenReturn(1L);

        CareerSkillGapResponse gap = mock(CareerSkillGapResponse.class);
        when(gap.getSkillId()).thenReturn(10L);

        CareerRecommendationResponse recommendation =
                mock(CareerRecommendationResponse.class);

        when(recommendation.getProficiencyGaps())
                .thenReturn(new ArrayList<>(List.of(gap)));

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(careerRepository.findById(1L))
                .thenReturn(Optional.of(career));

        when(recommendationService.getSkillGap(1L, email))
                .thenReturn(recommendation);

        when(roadmapRepository.save(any(Roadmap.class)))
                .thenReturn(roadmap);

        when(skillRepository.findById(10L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> roadmapService.createRoadmap(email, request)
        );

        verify(skillRepository).findById(10L);
        verify(roadmapRepository).save(any(Roadmap.class));
        verify(roadmapItemRepository, never()).save(any());
    }

    // 6. Get all roadmaps of a student

    @Test
    void getMyRoadmapsShouldReturnStudentRoadmaps() {

        when(roadmapRepository
                .findAllByUser_EmailOrderByCreatedAtDesc(email))
                .thenReturn(List.of(roadmap));

        when(roadmapItemRepository
                .findAllByRoadmap_IdOrderByOrderIndexAsc(100L))
                .thenReturn(List.of());

        List<RoadmapResponse> response =
                roadmapService.getMyRoadmaps(email);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Java Developer Learning Roadmap",
                response.get(0).getTitle());

        verify(roadmapRepository)
                .findAllByUser_EmailOrderByCreatedAtDesc(email);
    }

    // 7. Get a particular roadmap successfully

    @Test
    void getRoadmapShouldReturnOwnedRoadmap() {

        when(roadmapRepository.findByIdAndUser_Email(100L, email))
                .thenReturn(Optional.of(roadmap));

        when(roadmapItemRepository
                .findAllByRoadmap_IdOrderByOrderIndexAsc(100L))
                .thenReturn(List.of());

        RoadmapResponse response =
                roadmapService.getRoadmap(100L, email);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals("Java Developer Learning Roadmap",
                response.getTitle());
    }

    // 8. Get roadmap should fail when it does not belong to the user

    @Test
    void getRoadmapShouldFailWhenRoadmapNotFound() {

        when(roadmapRepository.findByIdAndUser_Email(100L, email))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> roadmapService.getRoadmap(100L, email)
        );
    }

    // 9. Update roadmap item status to completed

    @Test
    void updateItemStatusShouldMarkItemCompleted() {

        RoadmapItem item = new RoadmapItem();
        item.setId(200L);
        item.setRoadmap(roadmap);
        item.setSkill(skill);
        item.setOrderIndex(1);
        item.setCurrentLevel(SkillLevel.BEGINNER);
        item.setTargetLevel(SkillLevel.INTERMEDIATE);
        item.setEstimatedHours(20);
        item.setStatus(RoadmapItemStatus.PENDING);

        UpdateRoadmapItemStatusRequest request =
                mock(UpdateRoadmapItemStatusRequest.class);

        when(request.getStatus())
                .thenReturn(RoadmapItemStatus.COMPLETED);

        when(roadmapRepository.findByIdAndUser_Email(100L, email))
                .thenReturn(Optional.of(roadmap));

        when(roadmapItemRepository.findByIdAndRoadmap_Id(200L, 100L))
                .thenReturn(Optional.of(item));

        when(roadmapItemRepository.save(item))
                .thenReturn(item);

        RoadmapItemResponse response =
                roadmapService.updateItemStatus(
                        100L, 200L, email, request
                );

        assertNotNull(response);
        assertEquals(RoadmapItemStatus.COMPLETED, response.getStatus());
        assertNotNull(item.getCompletedAt());

        verify(roadmapItemRepository).save(item);
    }

    // 10. Update roadmap item status back to pending

    @Test
    void updateItemStatusShouldClearCompletedAtWhenNotCompleted() {

        RoadmapItem item = new RoadmapItem();
        item.setId(200L);
        item.setRoadmap(roadmap);
        item.setSkill(skill);
        item.setStatus(RoadmapItemStatus.COMPLETED);

        UpdateRoadmapItemStatusRequest request =
                mock(UpdateRoadmapItemStatusRequest.class);

        when(request.getStatus())
                .thenReturn(RoadmapItemStatus.PENDING);

        when(roadmapRepository.findByIdAndUser_Email(100L, email))
                .thenReturn(Optional.of(roadmap));

        when(roadmapItemRepository.findByIdAndRoadmap_Id(200L, 100L))
                .thenReturn(Optional.of(item));

        when(roadmapItemRepository.save(item))
                .thenReturn(item);

        RoadmapItemResponse response =
                roadmapService.updateItemStatus(
                        100L, 200L, email, request
                );

        assertNotNull(response);
        assertEquals(RoadmapItemStatus.PENDING, response.getStatus());
        assertNull(item.getCompletedAt());
    }

    // 11. Update should fail when roadmap item does not exist

    @Test
    void updateItemStatusShouldFailWhenItemNotFound() {

        UpdateRoadmapItemStatusRequest request =
                mock(UpdateRoadmapItemStatusRequest.class);

        when(roadmapRepository.findByIdAndUser_Email(100L, email))
                .thenReturn(Optional.of(roadmap));

        when(roadmapItemRepository.findByIdAndRoadmap_Id(200L, 100L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> roadmapService.updateItemStatus(
                        100L, 200L, email, request
                )
        );

        verify(roadmapItemRepository, never()).save(any());
    }
}
