package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.ResumeResponse;
import com.utkarsh.careerroadmap.entity.Resume;
import com.utkarsh.careerroadmap.entity.User;
import com.utkarsh.careerroadmap.exception.APIException;
import com.utkarsh.careerroadmap.repository.ResumeRepository;
import com.utkarsh.careerroadmap.repository.UserRepository;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

@Service
public class ResumeService {
    @Autowired
    FileService fileService;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository ;

    public ResumeService(
            ResumeRepository resumeRepository,
            UserRepository userRepository
       ) {

        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;

    }

    // Upload resume
    @Transactional
    public ResumeResponse uploadResume(
            String email,
            MultipartFile file) throws IOException {

        // Check whether file exists
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a file"
            );
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File size cannot exceed 5 MB"
            );
        }

        // Check original filename
        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null ||
                !originalFileName.toLowerCase().endsWith(".pdf")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF files are allowed"
            );
        }

        // Verify PDF file signature
        try {
            byte[] signature = file.getInputStream().readNBytes(5);
            if (signature.length != 5 ||
                    signature[0] != '%' ||
                    signature[1] != 'P' ||
                    signature[2] != 'D' ||
                    signature[3] != 'F' ||
                    signature[4] != '-') {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid PDF file"
                );
            }

        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Unable to read uploaded file"
            );
        }

        // Find logged-in user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        String fileName = fileService.uploadfile(file);
        Resume resume = new Resume();

        resume.setUser(user);
        resume.setOriginalFileName(
                Paths.get(originalFileName.replace("\\", "/"))
                        .getFileName()
                        .toString()
        );
        resume.setStoredFileName(fileName);
        resume.setContentType("application/pdf");
        resume.setFileSize(file.getSize());

        Resume savedResume = resumeRepository.save(resume);

        return convertToResponse(savedResume);
    }

    // Convert entity into response DTO
    private ResumeResponse convertToResponse(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getOriginalFileName(),
                resume.getContentType(),
                resume.getFileSize(),
                resume.getUploadedAt(),
                resume.getStoredFileName()
        );
    }

    //Get all resumes belonging to the logged-in user
    @Transactional(readOnly = true)
    public java.util.List<ResumeResponse> getMyResumes(String email) {

        return resumeRepository
                .findAllByUser_EmailOrderByUploadedAtDesc(email)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional
    public ResumeResponse updateLatestResume(
            String email,
            MultipartFile file
    ) throws IOException {

        // Validate uploaded file
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select a PDF file"
            );
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "File size cannot exceed 5 MB"
            );
        }

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null ||
                !originalFileName.toLowerCase().endsWith(".pdf")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only PDF files are allowed"
            );
        }

        // Verify PDF signature
        try (InputStream input = file.getInputStream()) {
            byte[] signature = input.readNBytes(5);

            if (signature.length != 5 ||
                    signature[0] != '%' ||
                    signature[1] != 'P' ||
                    signature[2] != 'D' ||
                    signature[3] != 'F' ||
                    signature[4] != '-') {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid PDF file"
                );
            }
        }

        // Fetch latest resume belonging to logged-in user
        Resume existingResume = resumeRepository
                .findFirstByUser_EmailOrderByUploadedAtDesc(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No existing resume found. Please upload a resume first."
                ));

        // Upload new PDF to Cloudinary
        String newFileUrl = fileService.uploadfile(file);

        // Update existing database record
        existingResume.setStoredFileName(newFileUrl);

        existingResume.setOriginalFileName(
                Paths.get(originalFileName.replace("\\", "/"))
                        .getFileName()
                        .toString()
        );

        existingResume.setContentType("application/pdf");
        existingResume.setFileSize(file.getSize());

        Resume updatedResume = resumeRepository.save(existingResume);

        return convertToResponse(updatedResume);
    }

}
