package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.ResumeResponse;
import com.utkarsh.careerroadmap.entity.Resume;
import com.utkarsh.careerroadmap.entity.User;
import com.utkarsh.careerroadmap.repository.ResumeRepository;
import com.utkarsh.careerroadmap.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
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

    @Mock
    private FileService fileService;

    @InjectMocks
    private ResumeService resumeService;

    private User user;
    private final String email = "student@example.com";

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail(email);
    }

    private MockMultipartFile createValidPdf() {
        byte[] content = "%PDF-1.4\nTest PDF content".getBytes();
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
        resume.setStoredFileName("https://res.cloudinary.com/test/resume.pdf");
        resume.setContentType("application/pdf");
        resume.setFileSize(100L);
        return resume;
    }

    @Test
    void uploadResumeShouldSaveResumeSuccessfully() throws IOException {
        MockMultipartFile file = createValidPdf();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(fileService.uploadfile(any())).thenReturn("https://res.cloudinary.com/test/resume.pdf");
        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResumeResponse response = resumeService.uploadResume(email, file);

        assertNotNull(response);
        assertEquals("resume.pdf", response.getOriginalFileName());
        assertEquals("application/pdf", response.getContentType());
        verify(userRepository).findByEmail(email);
        verify(resumeRepository).save(any(Resume.class));
    }

    @Test
    void uploadResumeShouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(
                ResponseStatusException.class,
                () -> resumeService.uploadResume(email, file)
        );
    }

    @Test
    void uploadResumeShouldRejectNonPdf() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "document.docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "test content".getBytes()
        );

        assertThrows(
                ResponseStatusException.class,
                () -> resumeService.uploadResume(email, file)
        );
    }

    @Test
    void getMyResumesShouldReturnList() {
        Resume resume = createResume();
        when(resumeRepository.findAllByUser_EmailOrderByUploadedAtDesc(email))
                .thenReturn(List.of(resume));

        List<ResumeResponse> responses = resumeService.getMyResumes(email);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        assertEquals("resume.pdf", responses.get(0).getOriginalFileName());
        verify(resumeRepository).findAllByUser_EmailOrderByUploadedAtDesc(email);
    }

    @Test
    void updateLatestResumeShouldUpdateWhenResumeExists() throws IOException {
        Resume existing = createResume();
        MockMultipartFile file = createValidPdf();

        when(resumeRepository.findFirstByUser_EmailOrderByUploadedAtDesc(email))
                .thenReturn(Optional.of(existing));
        when(fileService.uploadfile(any())).thenReturn("https://res.cloudinary.com/test/new.pdf");
        when(resumeRepository.save(any(Resume.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResumeResponse response = resumeService.updateLatestResume(email, file);

        assertNotNull(response);
        verify(resumeRepository).save(existing);
    }
}
