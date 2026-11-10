
package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.CreateCareerRequest;
import com.utkarsh.careerroadmap.dto.CareerResponse;
import com.utkarsh.careerroadmap.entity.Career;
import com.utkarsh.careerroadmap.repository.CareerRepository;
import com.utkarsh.careerroadmap.repository.CareerSkillRepository;
import com.utkarsh.careerroadmap.repository.SkillRepository;

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
class CareerServiceTest {

    @Mock
    private CareerRepository careerRepository;

    @Mock
    private CareerSkillRepository careerSkillRepository;

    @Mock
    private SkillRepository skillRepository;

    @InjectMocks
    private CareerService careerService;

    private CreateCareerRequest request;
    private Career career;

    @BeforeEach
    void setUp() {

        request = new CreateCareerRequest();
        request.setName("Java Backend Developer");
        request.setCategory("Software Development");
        request.setDescription("Develop backend applications using Java.");

        career = new Career();
        career.setId(1L);
        career.setName("Java Backend Developer");
        career.setCategory("Software Development");
        career.setDescription("Develop backend applications using Java.");
    }

    @Test
    void createCareerShouldSaveCareerSuccessfully() {

        when(careerRepository.save(any(Career.class)))
                .thenAnswer(invocation -> {
                    Career saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        CareerResponse response = careerService.createCareer(request);

        assertNotNull(response);
        assertEquals("Java Backend Developer", response.getName());
        assertEquals("Software Development", response.getCategory());

        verify(careerRepository).save(any(Career.class));
    }

    @Test
    void getAllCareersShouldReturnCareerList() {

        when(careerRepository.findAll())
                .thenReturn(List.of(career));

        List<CareerResponse> response = careerService.getAllCareers();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Java Backend Developer", response.get(0).getName());
    }

    @Test
    void getAllCareersShouldReturnEmptyListWhenNoCareersExist() {

        when(careerRepository.findAll())
                .thenReturn(List.of());

        List<CareerResponse> response = careerService.getAllCareers();

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    @Test
    void getCareerByIdShouldReturnCareer() {

        when(careerRepository.findById(1L))
                .thenReturn(Optional.of(career));

        CareerResponse response = careerService.getCareerById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Java Backend Developer", response.getName());
    }

    @Test
    void getCareerByIdShouldFailWhenCareerDoesNotExist() {

        when(careerRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> careerService.getCareerById(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}
