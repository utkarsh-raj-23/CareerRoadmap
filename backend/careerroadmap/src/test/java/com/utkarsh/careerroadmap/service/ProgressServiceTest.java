
package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.ProgressSummaryResponse;
import com.utkarsh.careerroadmap.entity.Roadmap;
import com.utkarsh.careerroadmap.entity.RoadmapItem;
import com.utkarsh.careerroadmap.entity.User;
import com.utkarsh.careerroadmap.enums.RoadmapItemStatus;
import com.utkarsh.careerroadmap.repository.RoadmapItemRepository;
import com.utkarsh.careerroadmap.repository.RoadmapRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceTest {

    @Mock
    private RoadmapRepository roadmapRepository;

    @Mock
    private RoadmapItemRepository roadmapItemRepository;

    @InjectMocks
    private ProgressService progressService;

    private User user;
    private Roadmap roadmap;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");

        roadmap = new Roadmap();
        roadmap.setId(10L);
        roadmap.setUser(user);
        roadmap.setTitle("Java Backend Roadmap");
    }

    private RoadmapItem createItem(
            RoadmapItemStatus status,
            int hours) {

        RoadmapItem item = new RoadmapItem();
        item.setRoadmap(roadmap);
        item.setStatus(status);
        item.setEstimatedHours(hours);

        return item;
    }

    @Test
    void getRoadmapProgressShouldCalculateCorrectPercentage() {

        List<RoadmapItem> items = List.of(
                createItem(RoadmapItemStatus.COMPLETED, 10),
                createItem(RoadmapItemStatus.COMPLETED, 15),
                createItem(RoadmapItemStatus.IN_PROGRESS, 20),
                createItem(RoadmapItemStatus.PENDING, 10),
                createItem(RoadmapItemStatus.PENDING, 5)
        );

        when(roadmapRepository.findByIdAndUser_Email(
                10L, "test@gmail.com"))
                .thenReturn(Optional.of(roadmap));

        when(roadmapItemRepository.findAllByRoadmap_IdOrderByOrderIndexAsc(10L))
                .thenReturn(items);

        ProgressSummaryResponse response =
                progressService.getRoadmapProgress(10L, "test@gmail.com");

        assertNotNull(response);

        assertEquals(10L, response.getRoadmapId());
        assertEquals("Java Backend Roadmap", response.getRoadmapTitle());

        assertEquals(5, response.getTotalTasks());
        assertEquals(2, response.getCompletedTasks());
        assertEquals(1, response.getInProgressTasks());
        assertEquals(2, response.getPendingTasks());

        assertEquals(60, response.getTotalEstimatedHours());
        assertEquals(25, response.getCompletedEstimatedHours());

        assertEquals(40.0, response.getCompletionPercentage(), 0.01);
    }

    @Test
    void getRoadmapProgressShouldReturnZeroWhenNoTasksExist() {

        when(roadmapRepository.findByIdAndUser_Email(
                10L, "test@gmail.com"))
                .thenReturn(Optional.of(roadmap));

        when(roadmapItemRepository.findAllByRoadmap_IdOrderByOrderIndexAsc(10L))
                .thenReturn(List.of());

        ProgressSummaryResponse response =
                progressService.getRoadmapProgress(10L, "test@gmail.com");

        assertNotNull(response);
        assertEquals(0, response.getTotalTasks());
        assertEquals(0, response.getCompletedTasks());
        assertEquals(0.0, response.getCompletionPercentage(), 0.01);
    }

    @Test
    void getRoadmapProgressShouldFailWhenRoadmapDoesNotExist() {

        when(roadmapRepository.findByIdAndUser_Email(
                999L, "test@gmail.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> progressService.getRoadmapProgress(
                        999L, "test@gmail.com")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void getOverallProgressShouldCalculateAllRoadmaps() {

        Roadmap secondRoadmap = new Roadmap();
        secondRoadmap.setId(20L);
        secondRoadmap.setUser(user);
        secondRoadmap.setTitle("Spring Boot Roadmap");

        when(roadmapRepository.findAllByUser_EmailOrderByCreatedAtDesc(
                "test@gmail.com"))
                .thenReturn(List.of(roadmap, secondRoadmap));

        when(roadmapItemRepository.findAllByRoadmap_IdOrderByOrderIndexAsc(10L))
                .thenReturn(List.of(
                        createItem(RoadmapItemStatus.COMPLETED, 10),
                        createItem(RoadmapItemStatus.PENDING, 20)
                ));

        RoadmapItem secondCompleted = new RoadmapItem();
        secondCompleted.setRoadmap(secondRoadmap);
        secondCompleted.setStatus(RoadmapItemStatus.COMPLETED);
        secondCompleted.setEstimatedHours(15);

        when(roadmapItemRepository.findAllByRoadmap_IdOrderByOrderIndexAsc(20L))
                .thenReturn(List.of(secondCompleted));

        ProgressSummaryResponse response =
                progressService.getOverallProgress("test@gmail.com");

        assertNotNull(response);

        assertEquals(3, response.getTotalTasks());
        assertEquals(2, response.getCompletedTasks());
        assertEquals(1, response.getPendingTasks());

        assertEquals(45, response.getTotalEstimatedHours());
        assertEquals(25, response.getCompletedEstimatedHours());

        assertEquals(66.67, response.getCompletionPercentage(), 0.01);
    }
}
