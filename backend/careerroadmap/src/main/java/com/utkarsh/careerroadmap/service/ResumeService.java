package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.ResumeResponse;
import com.utkarsh.careerroadmap.entity.Resume;
import com.utkarsh.careerroadmap.entity.User;
import com.utkarsh.careerroadmap.repository.ResumeRepository;
import com.utkarsh.careerroadmap.repository.UserRepository;

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

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private final ResumeRepository resumeRepository;
    private final UserRepository userRepository;
    private final Path uploadDirectory;

    public ResumeService(
            ResumeRepository resumeRepository,
            UserRepository userRepository,
            @Value("${app.upload.dir}") String uploadDir) {

        this.resumeRepository = resumeRepository;
        this.userRepository = userRepository;
        this.uploadDirectory = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();
    }

    // Upload resume
    @Transactional
    public ResumeResponse uploadResume(String email, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a file");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size cannot exceed 5 MB");
        }
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || !originalFileName.toLowerCase().endsWith(".pdf")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only PDF files are allowed");
        }
        try {
            byte[] signature = file.getInputStream().readNBytes(5);
            if (signature.length != 5 || signature[0] != '%' || signature[1] != 'P' || signature[2] != 'D' || signature[3] != 'F' || signature[4] != '-') {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid PDF file");
            }
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to read uploaded file");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        String storedFileName = UUID.randomUUID() + ".pdf";
        Path targetPath = uploadDirectory.resolve(storedFileName).normalize();
        if (!targetPath.startsWith(uploadDirectory)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
        }
        try {
            Files.createDirectories(uploadDirectory);
            try (var is = file.getInputStream()) {
                Files.copy(is, targetPath);
            }
            Resume resume = new Resume();
            resume.setUser(user);
            resume.setOriginalFileName(Paths.get(originalFileName.replace("\\", "/")).getFileName().toString());
            resume.setStoredFileName(storedFileName);
            resume.setStoragePath(targetPath.toString());
            resume.setContentType("application/pdf");
            resume.setFileSize(file.getSize());
            Resume saved = resumeRepository.save(resume);
            return convertToResponse(saved);
        } catch (IOException ex) {
            deleteStoredFile(targetPath);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to save resume");
        } catch (RuntimeException ex) {
            deleteStoredFile(targetPath);
            throw ex;
        }
    }

    private ResumeResponse convertToResponse(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getOriginalFileName(),
                resume.getContentType(),
                resume.getFileSize(),
                resume.getUploadedAt()
        );
    }

    private void deleteStoredFile(Path path) {
        try { Files.deleteIfExists(path); } catch (IOException ignored) {}
    }

    @Transactional(readOnly = true)
    public java.util.List<ResumeResponse> getMyResumes(String email) {
        return resumeRepository.findAllByUser_EmailOrderByUploadedAtDesc(email)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public Resource downloadResume(Long id, String email) {
        Resume resume = resumeRepository.findByIdAndUser_Email(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));
        try {
            Path filePath = Paths.get(resume.getStoragePath()).toAbsolutePath().normalize();
            if (!filePath.startsWith(uploadDirectory)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume file not found");
            }
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume file not found");
            }
            return resource;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to download resume");
        }
    }

    @Transactional
    public void deleteResume(Long id, String email) {
        Resume resume = resumeRepository.findByIdAndUser_Email(id, email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Resume not found"));
        try {
            Path filePath = Paths.get(resume.getStoragePath()).toAbsolutePath().normalize();
            if (!filePath.startsWith(uploadDirectory)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file path");
            }
            Files.deleteIfExists(filePath);
            resumeRepository.delete(resume);
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unable to delete resume file");
        }
    }
}
