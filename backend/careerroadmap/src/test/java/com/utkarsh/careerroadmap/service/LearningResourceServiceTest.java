
package com.utkarsh.careerroadmap.service;

import java.util.List;
import java.util.Optional;

import com.utkarsh.careerroadmap.dto.LearningResourceResponse;
import com.utkarsh.careerroadmap.entity.LearningResource;
import com.utkarsh.careerroadmap.entity.Skill;
import com.utkarsh.careerroadmap.enums.ResourceType;
import com.utkarsh.careerroadmap.repository.LearningResourceRepository;
import com.utkarsh.careerroadmap.repository.SkillRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LearningResourceServiceTest {

    @Mock
    private LearningResourceRepository resourceRepository;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private LearningResourceService resourceService;

    private Skill skill;
    private LearningResource resource;

    @BeforeEach
    void setUp() {

        skill = new Skill();
        skill.setId(1L);
        skill.setName("Java");
        skill.setCategory("Programming");

        resource = new LearningResource();
        resource.setId(10L);
        resource.setSkill(skill);
        resource.setTitle("Java Complete Course");
        resource.setDescription("Learn Java programming.");
        resource.setUrl("https://example.com/java");
        resource.setProvider("Example");
        resource.setResourceType(ResourceType.COURSE);
        resource.setFree(true);
    }

    @Test
    void getAllResourcesShouldReturnResources() {

        when(resourceRepository.findAll())
                .thenReturn(List.of(resource));

        List<LearningResourceResponse> response =
                resourceService.getAllResources();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Java Complete Course", response.get(0).getTitle());
        assertEquals("Java", response.get(0).getSkillName());
        assertTrue(response.get(0).isFree());
    }

    @Test
    void getResourcesBySkillShouldReturnAllSkillResources() {

        when(skillRepository.existsById(1L)).thenReturn(true);

        when(resourceRepository.findAllBySkill_Id(1L))
                .thenReturn(List.of(resource));

        List<LearningResourceResponse> response =
                resourceService.getResourcesBySkill(1L, false);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Java Complete Course", response.get(0).getTitle());

        verify(skillRepository).existsById(1L);
        verify(resourceRepository).findAllBySkill_Id(1L);
    }

    @Test
    void getResourcesBySkillShouldReturnOnlyFreeResources() {

        when(skillRepository.existsById(1L)).thenReturn(true);

        when(resourceRepository.findAllBySkill_IdAndFreeTrue(1L))
                .thenReturn(List.of(resource));

        List<LearningResourceResponse> response =
                resourceService.getResourcesBySkill(1L, true);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertTrue(response.get(0).isFree());

        verify(skillRepository).existsById(1L);
        verify(resourceRepository).findAllBySkill_IdAndFreeTrue(1L);
    }

    @Test
    void getAllResourcesShouldReturnEmptyListWhenNoResourcesExist() {

        when(resourceRepository.findAll())
                .thenReturn(List.of());

        List<LearningResourceResponse> response =
                resourceService.getAllResources();

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(resourceRepository).findAll();
    }

    @Test
    void getResourcesBySkillShouldReturnEmptyListWhenNoResourcesExist() {

        when(skillRepository.existsById(1L)).thenReturn(true);

        when(resourceRepository.findAllBySkill_Id(1L))
                .thenReturn(List.of());

        List<LearningResourceResponse> response =
                resourceService.getResourcesBySkill(1L, false);

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(skillRepository).existsById(1L);
        verify(resourceRepository).findAllBySkill_Id(1L);
    }
}
