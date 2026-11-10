
package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.*;
import com.utkarsh.careerroadmap.entity.*;
import com.utkarsh.careerroadmap.enums.AssessmentStatus;
import com.utkarsh.careerroadmap.repository.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {

    @Mock
    private AssessmentRepository assessmentRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private QuestionOptionRepository optionRepository;

    @Mock
    private StudentAssessmentRepository studentAssessmentRepository;

    @Mock
    private StudentAnswerRepository studentAnswerRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AssessmentService assessmentService;

    private Assessment assessment;
    private User user;

    private final String email = "student@example.com";

    @BeforeEach
    void setUp() {
        assessment = new Assessment();
        assessment.setId(1L);
        assessment.setTitle("Java Assessment");
        assessment.setDescription("Java basics");
        assessment.setCategory("Programming");
        assessment.setTotalMarks(100);
        assessment.setDurationMinutes(30);
        assessment.setActive(true);

        user = new User();
        user.setEmail(email);
    }

    // 1. Create assessment successfully

    @Test
    void createAssessmentShouldSaveAssessmentSuccessfully() {

        CreateAssessmentRequest request =
                mock(CreateAssessmentRequest.class);

        when(request.getTitle()).thenReturn(" Java Assessment ");
        when(request.getDescription()).thenReturn("Java basics");
        when(request.getCategory()).thenReturn("Programming");
        when(request.getTotalMarks()).thenReturn(100);
        when(request.getDurationMinutes()).thenReturn(30);

        when(assessmentRepository.existsByTitleIgnoreCase(
                "Java Assessment")).thenReturn(false);

        when(assessmentRepository.save(any(Assessment.class)))
                .thenAnswer(invocation -> {
                    Assessment saved = invocation.getArgument(0);
                    saved.setId(1L);
                    return saved;
                });

        AssessmentResponse response =
                assessmentService.createAssessment(request);

        assertNotNull(response);
        assertEquals("Java Assessment", response.getTitle());

        verify(assessmentRepository).save(any(Assessment.class));
    }

    // 2. Duplicate assessment should be rejected

    @Test
    void createAssessmentShouldRejectDuplicateTitle() {

        CreateAssessmentRequest request =
                mock(CreateAssessmentRequest.class);

        when(request.getTitle()).thenReturn("Java Assessment");

        when(assessmentRepository.existsByTitleIgnoreCase(
                "Java Assessment")).thenReturn(true);

        assertThrows(
                ResponseStatusException.class,
                () -> assessmentService.createAssessment(request)
        );

        verify(assessmentRepository, never()).save(any());
    }

    // 3. Get all active assessments

    @Test
    void getAllAssessmentsShouldReturnActiveAssessments() {

        when(assessmentRepository.findAllByActiveTrue())
                .thenReturn(List.of(assessment));

        List<AssessmentResponse> response =
                assessmentService.getAllAssessments();

        assertNotNull(response);
        assertEquals(1, response.size());
        assertEquals("Java Assessment", response.get(0).getTitle());

        verify(assessmentRepository).findAllByActiveTrue();
    }

    // 4. Add question successfully

    @Test
    void addQuestionShouldSaveQuestionAndOptions() {

        AddQuestionRequest request = mock(AddQuestionRequest.class);

        when(request.getCorrectOptionIndex()).thenReturn(0);
        when(request.getOptions()).thenReturn(
                List.of("Java", "Python", "C++", "C")
        );
        when(request.getMarks()).thenReturn(5);
        when(request.getQuestionText())
                .thenReturn("Which language runs on JVM?");

        when(assessmentRepository.findById(1L))
                .thenReturn(Optional.of(assessment));

        when(questionRepository.countByAssessment_Id(1L))
                .thenReturn(0L);

        when(questionRepository
                .findAllByAssessment_IdOrderByOrderIndexAsc(1L))
                .thenReturn(List.of());

        when(questionRepository.save(any(Question.class)))
                .thenAnswer(invocation -> {
                    Question question = invocation.getArgument(0);
                    question.setId(10L);
                    return question;
                });

        when(optionRepository.save(any(QuestionOption.class)))
                .thenAnswer(invocation -> {
                    QuestionOption option = invocation.getArgument(0);
                    return option;
                });

        QuestionResponse response =
                assessmentService.addQuestion(1L, request);

        assertNotNull(response);
        assertEquals("Which language runs on JVM?",
                response.getQuestionText());
        assertEquals(5, response.getMarks());
        assertEquals(1, response.getOrderIndex());
        assertEquals(4, response.getOptions().size());

        verify(questionRepository).save(any(Question.class));
        verify(optionRepository, times(4)).save(any(QuestionOption.class));
    }

    // 5. Adding question to inactive assessment should fail

    @Test
    void addQuestionShouldRejectInactiveAssessment() {

        assessment.setActive(false);

        when(assessmentRepository.findById(1L))
                .thenReturn(Optional.of(assessment));

        AddQuestionRequest request = mock(AddQuestionRequest.class);

        assertThrows(
                ResponseStatusException.class,
                () -> assessmentService.addQuestion(1L, request)
        );

        verify(questionRepository, never()).save(any());
    }

    // 6. Get assessment questions

    @Test
    void getAssessmentQuestionsShouldReturnQuestions() {

        Question question = new Question();
        question.setId(10L);
        question.setAssessment(assessment);
        question.setQuestionText("What is JVM?");
        question.setMarks(5);
        question.setOrderIndex(1);

        QuestionOption option = new QuestionOption();
        option.setId(20L);
        option.setQuestion(question);
        option.setOptionText("Java Virtual Machine");
        option.setCorrect(true);

        when(assessmentRepository.findById(1L))
                .thenReturn(Optional.of(assessment));

        when(questionRepository
                .findAllByAssessment_IdOrderByOrderIndexAsc(1L))
                .thenReturn(List.of(question));

        when(optionRepository.findAllByQuestion_Id(10L))
                .thenReturn(List.of(option));

        List<QuestionResponse> response =
                assessmentService.getAssessmentQuestions(1L);

        assertEquals(1, response.size());
        assertEquals("What is JVM?", response.get(0).getQuestionText());
        assertEquals(1, response.get(0).getOptions().size());

        verify(optionRepository).findAllByQuestion_Id(10L);
    }

    // 7. Starting an assessment successfully

    @Test
    void startAssessmentShouldCreateAttempt() {

        when(assessmentRepository.findById(1L))
                .thenReturn(Optional.of(assessment));

        when(questionRepository.countByAssessment_Id(1L))
                .thenReturn(2L);

        when(studentAssessmentRepository
                .existsByUser_EmailAndAssessment_IdAndStatus(
                        email, 1L, AssessmentStatus.IN_PROGRESS))
                .thenReturn(false);

        when(userRepository.findByEmail(email))
                .thenReturn(Optional.of(user));

        when(studentAssessmentRepository.save(
                any(StudentAssessment.class)))
                .thenAnswer(invocation -> {
                    StudentAssessment attempt = invocation.getArgument(0);
                    attempt.setId(100L);
                    return attempt;
                });

        StudentAssessmentResponse response =
                assessmentService.startAssessment(1L, email);

        assertNotNull(response);
        assertEquals(100L, response.getId());

        verify(studentAssessmentRepository)
                .save(any(StudentAssessment.class));
    }

    // 8. Starting an assessment with no questions should fail

    @Test
    void startAssessmentShouldFailWhenNoQuestionsExist() {

        when(assessmentRepository.findById(1L))
                .thenReturn(Optional.of(assessment));

        when(questionRepository.countByAssessment_Id(1L))
                .thenReturn(0L);

        assertThrows(
                ResponseStatusException.class,
                () -> assessmentService.startAssessment(1L, email)
        );

        verify(studentAssessmentRepository, never())
                .save(any());
    }

    // 9. Get student's assessment attempts

    @Test
    void getMyAttemptsShouldReturnAttempts() {

        StudentAssessment attempt = new StudentAssessment();
        attempt.setId(100L);
        attempt.setUser(user);
        attempt.setAssessment(assessment);
        attempt.setStatus(AssessmentStatus.IN_PROGRESS);

        when(studentAssessmentRepository
                .findAllByUser_EmailOrderByStartedAtDesc(email))
                .thenReturn(List.of(attempt));

        List<StudentAssessmentResponse> response =
                assessmentService.getMyAttempts(email);

        assertEquals(1, response.size());
        assertEquals(100L, response.get(0).getId());

        verify(studentAssessmentRepository)
                .findAllByUser_EmailOrderByStartedAtDesc(email);
    }

    // 10. Get result of an assessment that is not completed

    @Test
    void getAssessmentResultShouldRejectIncompleteAttempt() {

        StudentAssessment attempt = new StudentAssessment();
        attempt.setId(100L);
        attempt.setAssessment(assessment);
        attempt.setStatus(AssessmentStatus.IN_PROGRESS);

        when(studentAssessmentRepository.findByIdAndUser_Email(
                100L, email))
                .thenReturn(Optional.of(attempt));

        assertThrows(
                ResponseStatusException.class,
                () -> assessmentService.getAssessmentResult(100L, email)
        );
    }

    // 11. Submit assessment successfully

    @Test
    void submitAssessmentShouldCalculateScore() {

        StudentAssessment attempt = new StudentAssessment();
        attempt.setId(100L);
        attempt.setUser(user);
        attempt.setAssessment(assessment);
        attempt.setStatus(AssessmentStatus.IN_PROGRESS);
        attempt.setStartedAt(LocalDateTime.now().minusMinutes(2));

        Question question = new Question();
        question.setId(10L);
        question.setAssessment(assessment);
        question.setQuestionText("What is JVM?");
        question.setMarks(5);

        QuestionOption selectedOption = new QuestionOption();
        selectedOption.setId(20L);
        selectedOption.setQuestion(question);
        selectedOption.setOptionText("Java Virtual Machine");
        selectedOption.setCorrect(true);

        AnswerSubmissionItem answerItem =
                mock(AnswerSubmissionItem.class);

        when(answerItem.getQuestionId()).thenReturn(10L);
        when(answerItem.getSelectedOptionId()).thenReturn(20L);

        SubmitAssessmentRequest request =
                mock(SubmitAssessmentRequest.class);

        when(request.getAnswers()).thenReturn(List.of(answerItem));

        when(studentAssessmentRepository.findByIdAndUser_Email(
                100L, email))
                .thenReturn(Optional.of(attempt));

        when(questionRepository.findById(10L))
                .thenReturn(Optional.of(question));

        when(optionRepository.findById(20L))
                .thenReturn(Optional.of(selectedOption));

        when(studentAnswerRepository.save(any(StudentAnswer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(studentAssessmentRepository.save(
                any(StudentAssessment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(studentAnswerRepository.findAllByStudentAssessment_Id(100L))
                .thenReturn(List.of());

        AssessmentResultResponse response =
                assessmentService.submitAssessment(100L, email, request);

        assertNotNull(response);
        assertEquals(5, attempt.getScore());
        assertEquals(AssessmentStatus.COMPLETED, attempt.getStatus());

        verify(studentAnswerRepository).save(any(StudentAnswer.class));
        verify(studentAssessmentRepository).save(attempt);
    }

    // 12. Submitting an already completed attempt should fail

    @Test
    void submitAssessmentShouldRejectCompletedAttempt() {

        StudentAssessment attempt = new StudentAssessment();
        attempt.setId(100L);
        attempt.setAssessment(assessment);
        attempt.setStatus(AssessmentStatus.COMPLETED);

        when(studentAssessmentRepository.findByIdAndUser_Email(
                100L, email))
                .thenReturn(Optional.of(attempt));

        SubmitAssessmentRequest request =
                mock(SubmitAssessmentRequest.class);

        assertThrows(
                ResponseStatusException.class,
                () -> assessmentService.submitAssessment(
                        100L, email, request)
        );

        verify(studentAnswerRepository, never()).save(any());
    }
}
