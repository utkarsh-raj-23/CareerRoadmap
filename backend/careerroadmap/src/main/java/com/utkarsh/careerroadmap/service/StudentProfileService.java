
package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.StudentProfileRequest;
import com.utkarsh.careerroadmap.dto.StudentProfileResponse;

import com.utkarsh.careerroadmap.entity.StudentProfile;
import com.utkarsh.careerroadmap.entity.User;

import com.utkarsh.careerroadmap.repository.StudentProfileRepository;
import com.utkarsh.careerroadmap.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentProfileService {

    private final StudentProfileRepository profileRepository;
    private final UserRepository userRepository;

    public StudentProfileService(
            StudentProfileRepository profileRepository,
            UserRepository userRepository) {

        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
    }

    // Create a new profile
    @Transactional
    public StudentProfileResponse createProfile(
            String email,
            StudentProfileRequest request) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (profileRepository.existsByUser_Email(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Profile already exists"
            );
        }

        StudentProfile profile = new StudentProfile();

        profile.setUser(user);
        updateProfileFields(profile, request);

        StudentProfile savedProfile =
                profileRepository.save(profile);

        return convertToResponse(savedProfile);
    }

    // Get the logged-in student's profile
    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(String email) {

        StudentProfile profile = profileRepository
                .findByUser_Email(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student profile not found"
                ));

        return convertToResponse(profile);
    }

    // Update the logged-in student's profile
    @Transactional
    public StudentProfileResponse updateProfile(
            String email,
            StudentProfileRequest request) {

        StudentProfile profile = profileRepository
                .findByUser_Email(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Student profile not found"
                ));

        updateProfileFields(profile, request);

        StudentProfile updatedProfile =
                profileRepository.save(profile);

        return convertToResponse(updatedProfile);
    }

    // Set profile fields from the request
    private void updateProfileFields(
            StudentProfile profile,
            StudentProfileRequest request) {

        profile.setCollege(request.getCollege());
        profile.setDegree(request.getDegree());
        profile.setBranch(request.getBranch());
        profile.setGraduationYear(request.getGraduationYear());
        profile.setCurrentSemester(request.getCurrentSemester());
        profile.setBio(request.getBio());
        profile.setGithubUrl(request.getGithubUrl());
        profile.setLinkedinUrl(request.getLinkedinUrl());
    }

    // Convert entity into response DTO
    private StudentProfileResponse convertToResponse(
            StudentProfile profile) {

        return new StudentProfileResponse(
                profile.getId(),
                profile.getUser().getFullName(),
                profile.getUser().getEmail(),
                profile.getCollege(),
                profile.getDegree(),
                profile.getBranch(),
                profile.getGraduationYear(),
                profile.getCurrentSemester(),
                profile.getBio(),
                profile.getGithubUrl(),
                profile.getLinkedinUrl()
        );
    }
}
