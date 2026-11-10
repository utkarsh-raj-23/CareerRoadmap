
package com.utkarsh.careerroadmap.service;

import com.utkarsh.careerroadmap.dto.*;
import com.utkarsh.careerroadmap.entity.*;

import com.utkarsh.careerroadmap.enums.AssessmentStatus;
import com.utkarsh.careerroadmap.enums.QuestionType;

import com.utkarsh.careerroadmap.repository.*;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository optionRepository;
    private final StudentAssessmentRepository studentAssessmentRepository;
    private final UserRepository userRepository;
    private final StudentAnswerRepository studentAnswerRepository;

    public AssessmentService(
            AssessmentRepository assessmentRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository optionRepository,
            StudentAssessmentRepository studentAssessmentRepository,
            StudentAnswerRepository studentAnswerRepository,
            UserRepository userRepository) {

        this.assessmentRepository = assessmentRepository;
        this.questionRepository = questionRepository;
        this.optionRepository = optionRepository;
        this.studentAssessmentRepository = studentAssessmentRepository;
        this.studentAnswerRepository = studentAnswerRepository;
        this.userRepository = userRepository;
    }

    // Create an assessment
    @Transactional
    public AssessmentResponse createAssessment(
            CreateAssessmentRequest request) {

        String title = request.getTitle().trim();

        if (assessmentRepository.existsByTitleIgnoreCase(title)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Assessment already exists"
            );
        }

        Assessment assessment = new Assessment();

        assessment.setTitle(title);
        assessment.setDescription(request.getDescription());
        assessment.setCategory(request.getCategory());
        assessment.setTotalMarks(request.getTotalMarks());
        assessment.setDurationMinutes(request.getDurationMinutes());
        assessment.setActive(true);

        Assessment saved = assessmentRepository.save(assessment);

        return convertAssessmentToResponse(saved);
    }

    // Get all active assessments
    @Transactional(readOnly = true)
    public List<AssessmentResponse> getAllAssessments() {

        List<Assessment> assessments =
                assessmentRepository.findAllByActiveTrue();

        List<AssessmentResponse> responses = new ArrayList<>();

        for (Assessment assessment : assessments) {
            responses.add(convertAssessmentToResponse(assessment));
        }

        return responses;
    }

    // Add an MCQ question and its options
    @Transactional
    public QuestionResponse addQuestion(
            Long assessmentId,
            AddQuestionRequest request) {

        Assessment assessment = assessmentRepository
                .findById(assessmentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Assessment not found"
                ));

        if (!assessment.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot add questions to an inactive assessment"
            );
        }

        if (request.getCorrectOptionIndex() >= request.getOptions().size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Correct option index is invalid"
            );
        }

        long questionCount =
                questionRepository.countByAssessment_Id(assessmentId);

        List<Question> existingQuestions =
                questionRepository
                        .findAllByAssessment_IdOrderByOrderIndexAsc(
                                assessmentId
                        );

        int marksAlreadyAssigned = 0;

        for (Question existingQuestion : existingQuestions) {
            marksAlreadyAssigned += existingQuestion.getMarks();
        }

        if (marksAlreadyAssigned + request.getMarks()
                > assessment.getTotalMarks()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Question marks exceed assessment total marks"
            );
        }

        Question question = new Question();

        question.setAssessment(assessment);
        question.setQuestionText(request.getQuestionText().trim());
        question.setQuestionType(QuestionType.MCQ);
        question.setMarks(request.getMarks());
        question.setOrderIndex((int) questionCount + 1);

        Question savedQuestion = questionRepository.save(question);

        List<QuestionOptionResponse> optionResponses =
                new ArrayList<>();


        for (int i = 0; i < request.getOptions().size(); i++) {

            QuestionOption option = new QuestionOption();

            option.setQuestion(savedQuestion);
            option.setOptionText(request.getOptions().get(i).trim());
            option.setCorrect(i == request.getCorrectOptionIndex());

            QuestionOption savedOption =
                    optionRepository.save(option);

            optionResponses.add(
                    new QuestionOptionResponse(
                            savedOption.getId(),
                            savedOption.getOptionText()
                    )
            );
        }


        return new QuestionResponse(
                savedQuestion.getId(),
                savedQuestion.getQuestionText(),
                savedQuestion.getMarks(),
                savedQuestion.getOrderIndex(),
                optionResponses
        );
    }

    // Get questions of an assessment without revealing correct answers
    @Transactional(readOnly = true)
    public List<QuestionResponse> getAssessmentQuestions(
            Long assessmentId) {

        Assessment assessment = assessmentRepository
                .findById(assessmentId)
                .filter(Assessment::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active assessment not found"
                ));

        List<Question> questions =
                questionRepository
                        .findAllByAssessment_IdOrderByOrderIndexAsc(
                                assessment.getId()
                        );

        List<QuestionResponse> responses = new ArrayList<>();

        for (Question question : questions) {

            List<QuestionOption> options =
                    optionRepository.findAllByQuestion_Id(question.getId());

            List<QuestionOptionResponse> optionResponses =
                    new ArrayList<>();

            for (QuestionOption option : options) {
                optionResponses.add(
                        new QuestionOptionResponse(
                                option.getId(),
                                option.getOptionText()
                        )
                );
            }

            responses.add(
                    new QuestionResponse(
                            question.getId(),
                            question.getQuestionText(),
                            question.getMarks(),
                            question.getOrderIndex(),
                            optionResponses
                    )
            );
        }

        return responses;
    }

    // Start an assessment attempt
    @Transactional
    public StudentAssessmentResponse startAssessment(
            Long assessmentId,
            String email) {

        Assessment assessment = assessmentRepository
                .findById(assessmentId)
                .filter(Assessment::isActive)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active assessment not found"
                ));

        if (questionRepository.countByAssessment_Id(assessmentId) == 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This assessment has no questions yet"
            );
        }

        boolean alreadyInProgress =
                studentAssessmentRepository
                        .existsByUser_EmailAndAssessment_IdAndStatus(
                                email,
                                assessmentId,
                                AssessmentStatus.IN_PROGRESS
                        );

        if (alreadyInProgress) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already have an assessment attempt in progress"
            );
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        StudentAssessment attempt = new StudentAssessment();

        attempt.setUser(user);
        attempt.setAssessment(assessment);
        attempt.setStatus(AssessmentStatus.IN_PROGRESS);

        StudentAssessment saved =
                studentAssessmentRepository.save(attempt);

        return convertStudentAssessmentToResponse(saved);
    }

    // Get student's assessment attempts
    @Transactional(readOnly = true)
    public List<StudentAssessmentResponse> getMyAttempts(String email) {

        List<StudentAssessment> attempts =
                studentAssessmentRepository
                        .findAllByUser_EmailOrderByStartedAtDesc(email);

        List<StudentAssessmentResponse> responses =
                new ArrayList<>();

        for (StudentAssessment attempt : attempts) {
            responses.add(
                    convertStudentAssessmentToResponse(attempt)
            );
        }

        return responses;
    }

    //Submit answers and calculate marks

    @Transactional
    public AssessmentResultResponse submitAssessment(
            Long attemptId,
            String email,
            SubmitAssessmentRequest request) {

        StudentAssessment attempt =
                studentAssessmentRepository
                        .findByIdAndUser_Email(attemptId, email)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Assessment attempt not found"
                        ));

        if (attempt.getStatus() != AssessmentStatus.IN_PROGRESS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This assessment has already been completed"
            );
        }

        Assessment assessment = attempt.getAssessment();

        // Check whether the time limit has expired
        long elapsedMinutes = Duration.between(
                attempt.getStartedAt(),
                LocalDateTime.now()
        ).toMinutes();

        if (elapsedMinutes >= assessment.getDurationMinutes()) {

            attempt.setScore(0);
            attempt.setStatus(AssessmentStatus.COMPLETED);
            attempt.setCompletedAt(LocalDateTime.now());

            studentAssessmentRepository.save(attempt);

            return new AssessmentResultResponse(
                    attempt.getId(),
                    assessment.getTitle(),
                    0,
                    assessment.getTotalMarks(),
                    0.0,
                    AssessmentStatus.COMPLETED,
                    attempt.getStartedAt(),
                    attempt.getCompletedAt(),
                    new ArrayList<>()
            );
        }

        Set<Long> submittedQuestionIds = new HashSet<>();

        int totalScore = 0;

        for (AnswerSubmissionItem item : request.getAnswers()) {

            Long questionId = item.getQuestionId();

            // Prevent duplicate answers for the same question
            if (!submittedQuestionIds.add(questionId)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Duplicate question submitted"
                );
            }

            Question question = questionRepository.findById(questionId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Question not found"
                    ));

            // Ensure the question belongs to this assessment
            if (!question.getAssessment().getId()
                    .equals(assessment.getId())) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Question does not belong to this assessment"
                );
            }

            QuestionOption selectedOption =
                    optionRepository.findById(item.getSelectedOptionId())
                            .orElseThrow(() -> new ResponseStatusException(
                                    HttpStatus.NOT_FOUND,
                                    "Selected option not found"
                            ));

            // Ensure the selected option belongs to this question
            if (!selectedOption.getQuestion().getId()
                    .equals(questionId)) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Selected option does not belong to this question"
                );
            }

            boolean isCorrect = selectedOption.isCorrect();

            int marksAwarded = isCorrect ? question.getMarks() : 0;

            totalScore += marksAwarded;

            StudentAnswer studentAnswer = new StudentAnswer();

            studentAnswer.setStudentAssessment(attempt);
            studentAnswer.setQuestion(question);
            studentAnswer.setSelectedOption(selectedOption);
            studentAnswer.setCorrect(isCorrect);
            studentAnswer.setMarksAwarded(marksAwarded);

            studentAnswerRepository.save(studentAnswer);
        }

        // Complete the assessment
        attempt.setScore(totalScore);
        attempt.setStatus(AssessmentStatus.COMPLETED);
        attempt.setCompletedAt(LocalDateTime.now());

        studentAssessmentRepository.save(attempt);

        return getAssessmentResult(attemptId, email);
    }


    // Retrieve the detailed assessment result
    @Transactional(readOnly = true)
    public AssessmentResultResponse getAssessmentResult(
            Long attemptId,
            String email) {

        StudentAssessment attempt =
                studentAssessmentRepository
                        .findByIdAndUser_Email(attemptId, email)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Assessment attempt not found"
                        ));

        if (attempt.getStatus() != AssessmentStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Assessment has not been completed yet"
            );
        }

        List<StudentAnswer> savedAnswers =
                studentAnswerRepository
                        .findAllByStudentAssessment_Id(attemptId);

        List<AnswerResultResponse> answerResponses =
                new ArrayList<>();

        for (StudentAnswer answer : savedAnswers) {

            Question question = answer.getQuestion();

            List<QuestionOption> options =
                    optionRepository.findAllByQuestion_Id(question.getId());

            String correctOptionText = null;

            for (QuestionOption option : options) {
                if (option.isCorrect()) {
                    correctOptionText = option.getOptionText();
                    break;
                }
            }

            String selectedOptionText = null;

            if (answer.getSelectedOption() != null) {
                selectedOptionText =
                        answer.getSelectedOption().getOptionText();
            }

            answerResponses.add(
                    new AnswerResultResponse(
                            question.getId(),
                            question.getQuestionText(),
                            selectedOptionText,
                            correctOptionText,
                            answer.isCorrect(),
                            answer.getMarksAwarded(),
                            question.getMarks()
                    )
            );
        }

        int score = attempt.getScore() == null ? 0 : attempt.getScore();

        double percentage =
                (score * 100.0) / attempt.getAssessment().getTotalMarks();

        return new AssessmentResultResponse(
                attempt.getId(),
                attempt.getAssessment().getTitle(),
                score,
                attempt.getAssessment().getTotalMarks(),
                Math.round(percentage * 100.0) / 100.0,
                attempt.getStatus(),
                attempt.getStartedAt(),
                attempt.getCompletedAt(),
                answerResponses
        );
    }


    private AssessmentResponse convertAssessmentToResponse(
            Assessment assessment) {

        return new AssessmentResponse(
                assessment.getId(),
                assessment.getTitle(),
                assessment.getDescription(),
                assessment.getCategory(),
                assessment.getTotalMarks(),
                assessment.getDurationMinutes()
        );
    }

    private StudentAssessmentResponse convertStudentAssessmentToResponse(
            StudentAssessment attempt) {

        return new StudentAssessmentResponse(
                attempt.getId(),
                attempt.getAssessment().getId(),
                attempt.getAssessment().getTitle(),
                attempt.getStartedAt(),
                attempt.getCompletedAt(),
                attempt.getScore(),
                attempt.getStatus()
        );
    }
}
