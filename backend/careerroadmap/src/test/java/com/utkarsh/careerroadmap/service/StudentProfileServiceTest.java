
package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.StudentProfileRequest;
import com.utkarsh.careerroadmap.dto.StudentProfileResponse;
import com.utkarsh.careerroadmap.entity.StudentProfile;
import com.utkarsh.careerroadmap.entity.User;
import com.utkarsh.careerroadmap.repository.StudentProfileRepository;
import com.utkarsh.careerroadmap.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentProfileServiceTest {

    @Mock
    private StudentProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StudentProfileService profileService;

    private User user;
    private StudentProfileRequest request;
    private StudentProfile profile;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1L);
        user.setFullName("Test Student");
        user.setEmail("test@gmail.com");

        request = new StudentProfileRequest();
        request.setCollege("NIT Patna");
        request.setDegree("B.Tech");
        request.setBranch("Computer Science");
        request.setGraduationYear(2028);
        request.setCurrentSemester(5);
        request.setBio("Computer Science student");
        request.setGithubUrl("https://github.com/test");
        request.setLinkedinUrl("https://linkedin.com/in/test");

        profile = new StudentProfile();
        profile.setId(10L);
        profile.setUser(user);
        profile.setCollege("NIT Patna");
        profile.setDegree("B.Tech");
        profile.setBranch("Computer Science");
        profile.setGraduationYear(2028);
        profile.setCurrentSemester(5);
        profile.setBio("Computer Science student");
        profile.setGithubUrl("https://github.com/test");
        profile.setLinkedinUrl("https://linkedin.com/in/test");
    }

    @Test
    void createProfileShouldSaveProfileSuccessfully() {

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(profileRepository.existsByUser_Email("test@gmail.com"))
                .thenReturn(false);

        when(profileRepository.save(any(StudentProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentProfileResponse response =
                profileService.createProfile("test@gmail.com", request);

        assertNotNull(response);
        assertEquals("NIT Patna", response.getCollege());
        assertEquals("B.Tech", response.getDegree());
        assertEquals("Computer Science", response.getBranch());
        assertEquals("test@gmail.com", response.getEmail());

        verify(profileRepository).save(any(StudentProfile.class));
    }

    @Test
    void createProfileShouldFailWhenUserDoesNotExist() {

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> profileService.createProfile("unknown@gmail.com", request)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(profileRepository, never()).save(any(StudentProfile.class));
    }

    @Test
    void createProfileShouldFailWhenProfileAlreadyExists() {

        when(userRepository.findByEmail("test@gmail.com"))
                .thenReturn(Optional.of(user));

        when(profileRepository.existsByUser_Email("test@gmail.com"))
                .thenReturn(true);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> profileService.createProfile("test@gmail.com", request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(profileRepository, never()).save(any(StudentProfile.class));
    }

    @Test
    void getProfileShouldReturnStudentProfile() {

        when(profileRepository.findByUser_Email("test@gmail.com"))
                .thenReturn(Optional.of(profile));

        StudentProfileResponse response =
                profileService.getProfile("test@gmail.com");

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("Test Student", response.getFullName());
        assertEquals("NIT Patna", response.getCollege());
        assertEquals(2028, response.getGraduationYear());
    }

    @Test
    void getProfileShouldFailWhenProfileDoesNotExist() {

        when(profileRepository.findByUser_Email("test@gmail.com"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> profileService.getProfile("test@gmail.com")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void updateProfileShouldUpdateExistingProfile() {

        request.setCollege("Updated College");
        request.setCurrentSemester(6);

        when(profileRepository.findByUser_Email("test@gmail.com"))
                .thenReturn(Optional.of(profile));

        when(profileRepository.save(any(StudentProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentProfileResponse response =
                profileService.updateProfile("test@gmail.com", request);

        assertNotNull(response);
        assertEquals("Updated College", response.getCollege());
        assertEquals(6, response.getCurrentSemester());

        verify(profileRepository).save(profile);
    }
}
