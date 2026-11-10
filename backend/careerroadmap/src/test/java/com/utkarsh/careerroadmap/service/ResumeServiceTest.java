
package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.ResumeResponse;
import com.utkarsh.careerroadmap.entity.Resume;
import com.utkarsh.careerroadmap.entity.User;
import com.utkarsh.careerroadmap.repository.ResumeRepository;
import com.utkarsh.careerroadmap.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResumeServiceTest {

    @Mock
    private ResumeRepository resumeRepository;

    @Mock
    private UserRepository userRepository;

    @TempDir
    Path tempDir;

    private ResumeService resumeService;

    private User user;

    private final String email = "student@example.com";

    @BeforeEach
    void setUp() {

        resumeService = new ResumeService(
                resumeRepository,
                userRepository,
                tempDir.toString()
        );

        user = new User();
        user.setEmail(email);
    }

    private MockMultipartFile createValidPdf() {

        byte[] content = "%PDF-1.4\nTest PDF content"
                .getBytes();

        return new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                content
        );
    }

    private Resume createResume() {

        Resume resume = new Resume();

        resume.setId(1L);
        resume.setUser(user);
        resume.setOriginalFileName("resume.pdf");
        resume.setStoredFileName("stored-resume.pdf");
        resume.setStoragePath(
                tempDir.resolve("stored-resume.pdf").toString()
        );
        resume.setContentType("application/pdf");
        resume.setFileSize(100L);

        return resume;
    }

    // 1. Successful resume upload

    @Test
    void uploadResumeShouldSaveResumeSuccessfully() {

        MockMultipartFile file = createValidPdf();

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(resumeRepository.save(any(Resume.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResumeResponse response =
                resumeService.uploadResume(email, file);

        assertNotNull(response);
        assertEquals("resume.pdf", response.getOriginalFileName());
        assertEquals("application/pdf", response.getContentType());

        verify(userRepository).findByEmail(email);
        verify(resumeRepository).save(any(Resume.class));
    }

    // 2. Upload should fail for empty file

    @Test
    void uploadResumeShouldRejectEmptyFile() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(
                ResponseStatusException.class,
                () -> resumeService.uploadResume(email, file)
        );

        verify(resumeRepository, never()).save(any());
    }

    // 3. Upload should reject non-PDF file

    @Test
    void uploadResumeShouldRejectInvalidFileType() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.txt",
                "text/plain",
                "This is not a PDF".getBytes()
        );

        assertThrows(
                ResponseStatusException.class,
                () -> resumeService.uploadResume(email, file)
        );

        verify(resumeRepository, never()).save(any());
    }

    // 4. Upload should reject invalid PDF signature

    @Test
    void uploadResumeShouldRejectInvalidPdfContent() {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "resume.pdf",
                "application/pdf",
                "This is not a real PDF".getBytes()
        );

        assertThrows(
                ResponseStatusException.class,
                () -> resumeService.uploadResume(email, file)
        );

        verify(resumeRepository, never()).save(any());
    }

    // 5. Upload should fail when user does not exist

    @Test
    void uploadResumeShouldFailWhenUserNotFound() {

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> resumeService.uploadResume(
                        email,
                        createValidPdf()
                )
        );

        verify(resumeRepository, never()).save(any());
    }

    // 6. Get all resumes of a user

    @Test
    void getMyResumesShouldReturnUserResumes() {

        Resume resume = createResume();

        when(resumeRepository
                .findAllByUser_EmailOrderByUploadedAtDesc(email))
                .thenReturn(List.of(resume));

        List<ResumeResponse> response =
                resumeService.getMyResumes(email);

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals(
                "resume.pdf",
                response.get(0).getOriginalFileName()
        );

        verify(resumeRepository)
                .findAllByUser_EmailOrderByUploadedAtDesc(email);
    }

    // 7. Get resumes should return empty list

    @Test
    void getMyResumesShouldReturnEmptyListWhenNoResumesExist() {

        when(resumeRepository
                .findAllByUser_EmailOrderByUploadedAtDesc(email))
                .thenReturn(List.of());

        List<ResumeResponse> response =
                resumeService.getMyResumes(email);

        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    // 8. Download resume successfully

    @Test
    void downloadResumeShouldReturnFile() throws Exception {

        Resume resume = createResume();

        Path filePath = tempDir.resolve("stored-resume.pdf");

        Files.write(
                filePath,
                "%PDF-1.4\nTest PDF".getBytes()
        );

        when(resumeRepository.findByIdAndUser_Email(1L, email))
                .thenReturn(Optional.of(resume));

        Resource resource =
                resumeService.downloadResume(1L, email);

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());

        verify(resumeRepository)
                .findByIdAndUser_Email(1L, email);
    }

    // 9. Download should fail when resume does not exist

    @Test
    void downloadResumeShouldFailWhenResumeNotFound() {

        when(resumeRepository.findByIdAndUser_Email(1L, email))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> resumeService.downloadResume(1L, email)
        );
    }

    // 10. Delete resume successfully

    @Test
    void deleteResumeShouldDeleteFileAndDatabaseRecord()
            throws Exception {

        Resume resume = createResume();

        Path filePath = tempDir.resolve("stored-resume.pdf");

        Files.write(filePath, "PDF content".getBytes());

        when(resumeRepository.findByIdAndUser_Email(1L, email))
                .thenReturn(Optional.of(resume));

        resumeService.deleteResume(1L, email);

        assertFalse(Files.exists(filePath));

        verify(resumeRepository)
                .findByIdAndUser_Email(1L, email);

        verify(resumeRepository).delete(resume);
    }

    // 11. Delete should fail when resume does not exist

    @Test
    void deleteResumeShouldFailWhenResumeNotFound() {

        when(resumeRepository.findByIdAndUser_Email(1L, email))
                .thenReturn(Optional.empty());

        assertThrows(
                ResponseStatusException.class,
                () -> resumeService.deleteResume(1L, email)
        );

        verify(resumeRepository, never()).delete(any());
    }
}
