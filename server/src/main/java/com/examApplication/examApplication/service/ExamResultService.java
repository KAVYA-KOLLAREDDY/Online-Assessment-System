package com.examApplication.examApplication.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.examApplication.examApplication.dto.DifficultyLevelStatsDTO;
import com.examApplication.examApplication.dto.ExamResultRequest;
import com.examApplication.examApplication.dto.ExamResultResponseDTO;
import com.examApplication.examApplication.dto.ExamResultTopicDTO;
import com.examApplication.examApplication.dto.QuestionAnalysisDTO;
import com.examApplication.examApplication.dto.StudentResponseRequest;
import com.examApplication.examApplication.dto.SubtopicDifficultyAnalysisDTO;
import com.examApplication.examApplication.dto.TopicResultDTO;
import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.ExamResult;
import com.examApplication.examApplication.entity.Question;
import com.examApplication.examApplication.entity.QuestionOption;
import com.examApplication.examApplication.entity.StudentAttempt;
import com.examApplication.examApplication.entity.StudentResponse;
import com.examApplication.examApplication.entity.SubtopicResult;
import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.model.AttemptStatus;
import com.examApplication.examApplication.model.QuestionType;
import com.examApplication.examApplication.repository.ExamRepository;
import com.examApplication.examApplication.repository.ExamResultRepository;
import com.examApplication.examApplication.repository.QuestionOptionRepository;
import com.examApplication.examApplication.repository.QuestionRepository;
import com.examApplication.examApplication.repository.StudentAttemptRepository;
import com.examApplication.examApplication.repository.StudentCertificateRepository;
import com.examApplication.examApplication.repository.StudentResponseRepository;
import com.examApplication.examApplication.repository.SubtopicResultRepository;
import com.examApplication.examApplication.repository.UserRepository;

import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class ExamResultService {

    private final QuestionRepository questionRepository;
    private final ExamResultRepository examResultRepository;
    private final StudentResponseRepository studentResponseRepository;
    private final StudentAttemptRepository studentAttemptRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final SubtopicResultRepository subTopicResultRepository;
    private final SubTopicService subtopicService;
    private final StudentAttemptService studentAttemptService;
    private final StudentCertificateService studentCertificateService;
    private final StudentCertificateRepository studentCertificateRepository;

    private static final int MAX_ATTEMPTS = 3;

    public ExamResultTopicDTO calcExamRes(ExamResultRequest request) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Invalid User Email"));
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Invalid Exam ID"));

        // ✅ Fetch all attempts sorted by attemptCount descending
        List<StudentAttempt> attempts = studentAttemptRepository.findByUserAndExamOrderByAttemptCountDesc(user, exam);
        if (attempts.isEmpty()) {
            throw new RuntimeException("No attempts found for this exam");
        }

        StudentAttempt studentAttempt = attempts.get(0);

        // ❌ Check if already maxed out or attempt is not ON_GOING
        if (studentAttempt.getAttemptCount() > MAX_ATTEMPTS ||
            studentAttempt.getStatus() != AttemptStatus.ON_GOING) {
            throw new RuntimeException("❌ You have exceeded the maximum allowed attempts for this exam.");
        }

        // ✅ Update attempt status to COMPLETED or VOILATED
        studentAttempt.setStatus(AttemptStatus.valueOf(request.getAttemptStatus()));
        studentAttempt = studentAttemptRepository.save(studentAttempt);


        // StudentAttempt studentAttempt = new StudentAttempt();
        // studentAttempt.setUser(user);
        // studentAttempt.setExam(exam);
        // studentAttempt.setAttemptCount(attemptCount + 1);
        // studentAttempt.setCreatedAt(LocalDateTime.now());
        // studentAttempt = studentAttemptRepository.save(studentAttempt);
        // System.out.println("New Attempt Created: ID=" + studentAttempt.getAttemptId() + ", AttemptCount=" + studentAttempt.getAttemptCount());

        List<StudentResponseRequest> responses = request.getResponses();
        System.out.println("Responses Received: " + responses.size());
        responses.forEach(resp -> System.out.println("Question: " + resp.getQuestion() + ", Options: " + resp.getSelectedOption()));

        // Maps for tracking
        Map<String, Integer> totalMarksByTopic = new HashMap<>();
        Map<String, Integer> totalQuestionsByTopic = new HashMap<>();
        Map<String, Double> obtainedMarksByTopic = new HashMap<>();
        Map<String, Integer> correctQuestionsByTopic = new HashMap<>();

        // Fetch all questions for the exam to validate response completeness
        // List<Question> allQuestions = questionRepository.findByExamExamId(request.getExamId());
        // if (responses.size() < allQuestions.size()) {
        //     System.out.println("Warning: Received " + responses.size() + " responses, expected " + allQuestions.size());
        // }

        // Process responses
        Set<Integer> processedQuestionIds = new HashSet<>();
        for (StudentResponseRequest response : responses) {
            Question question = questionRepository.findById(response.getQuestion())
                    .orElseThrow(() -> new RuntimeException("Question not found: ID=" + response.getQuestion()));
            Integer questionId = question.getQuestionId();

            // Skip duplicate responses
            if (!processedQuestionIds.add(questionId)) {
                System.out.println("Skipping duplicate response for Question ID: " + questionId);
                continue;
            }

            String subtopicName = question.getSubtopic().getName();
            totalMarksByTopic.merge(subtopicName, question.getMarks(), Integer::sum);
            totalQuestionsByTopic.merge(subtopicName, 1, Integer::sum);

            List<QuestionOption> selectedOptions = questionOptionRepository.findAllById(response.getSelectedOption());
            List<QuestionOption> correctOptions = questionOptionRepository.findByQuestionAndIsCorrect(question, true);

            // Save response (no selection)
            if (selectedOptions.isEmpty()) {
                StudentResponse studentResponse = new StudentResponse();
                studentResponse.setAttempt(studentAttempt);
                studentResponse.setQuestion(question);
                studentResponse.setSelectedOption(null);
                studentResponse.setSubmittedAt(LocalDateTime.now());
                studentResponseRepository.save(studentResponse);
            }

            // Save selected responses
            for (QuestionOption selectedOption : selectedOptions) {
                StudentResponse studentResponse = new StudentResponse();
                studentResponse.setAttempt(studentAttempt);
                studentResponse.setQuestion(question);
                studentResponse.setSelectedOption(selectedOption);
                studentResponse.setSubmittedAt(LocalDateTime.now());
                studentResponseRepository.save(studentResponse);
            }

            Set<Integer> selectedIds = selectedOptions.stream().map(QuestionOption::getOptionId).collect(Collectors.toSet());
            Set<Integer> correctIds = correctOptions.stream().map(QuestionOption::getOptionId).collect(Collectors.toSet());

            boolean isCorrect = question.getQuestionType() == QuestionType.MSQ
                    ? selectedIds.equals(correctIds)
                    : selectedOptions.size() == 1 && selectedOptions.get(0).getIsCorrect();

            if (isCorrect) {
                double obtainedMarks = question.getMarks();
                obtainedMarksByTopic.merge(subtopicName, obtainedMarks, Double::sum);
                correctQuestionsByTopic.merge(subtopicName, 1, Integer::sum);
            }
        }

        // Total exam marks
        int totalMarks = totalMarksByTopic.values().stream().reduce(0, Integer::sum);
        double obtMarks = obtainedMarksByTopic.values().stream().reduce(0.0, Double::sum);
        double percentage = totalMarks > 0 ? Math.round((obtMarks / totalMarks) * 100 * 100.0) / 100.0 : 0.0;
        boolean passed = percentage >= 40;

        // Save exam result
        ExamResult result = ExamResult.builder()
                .exam(exam)
                .user(user)
                .obtainedMarks(obtMarks)
                .totalMarks(totalMarks)
                .percentage(percentage)
                .passed(passed)
                .completedAt(LocalDateTime.now())
                .build();

        ExamResult savedExamResult = examResultRepository.save(result);
        System.out.println("Exam Result Saved: ID=" + savedExamResult.getResultId());
        
        if (passed) {
            boolean alreadyExists = studentCertificateRepository.existsByUserAndExam(user, exam);
            if (!alreadyExists) {
                studentCertificateService.generateAndStoreCertificate(user, exam);
                System.out.println("✅ Certificate generated and emailed");
            } else {
                System.out.println("⚠️ Certificate already exists, skipping generation");
            }
        }

        // Subtopic-wise results
        List<TopicResultDTO> topicWiseResults = new ArrayList<>();
        for (String subtopicName : totalQuestionsByTopic.keySet()) {
            Integer totalQuestions = totalQuestionsByTopic.getOrDefault(subtopicName, 0);
            Integer totalSubtopicMarks = totalMarksByTopic.getOrDefault(subtopicName, 0);
            Double obtained = obtainedMarksByTopic.getOrDefault(subtopicName, 0.0);
            double subtopicPercentage = totalSubtopicMarks > 0 ? Math.round((obtained / totalSubtopicMarks) * 100 * 100.0) / 100.0 : 0.0;
            boolean isPassed = subtopicPercentage >= 40;

            SubtopicResult subtopicResult = SubtopicResult.builder()
                    .examResult(savedExamResult)
                    .subtopic(subtopicService.getSubtopicByName(subtopicName))
                    .totalMarks(totalSubtopicMarks)
                    .percentage(subtopicPercentage)
                    .passed(isPassed)
                    .createdAt(LocalDateTime.now())
                    .build();

            subTopicResultRepository.save(subtopicResult);

            // Calculate correct questions for subtopic
            List<StudentResponse> subtopicResponses = studentResponseRepository
                    .findByAttemptAndQuestionSubtopic(studentAttempt, subtopicService.getSubtopicByName(subtopicName));
            System.out.println("Subtopic: " + subtopicName + ", Responses: " + subtopicResponses.size());

            // Group responses by question to handle MSQ correctly
            Map<Integer, List<StudentResponse>> responsesByQuestion = subtopicResponses.stream()
                    .collect(Collectors.groupingBy(resp -> resp.getQuestion().getQuestionId()));
            int correctQuestions = responsesByQuestion.entrySet().stream()
                    .filter(entry -> {
                        Question question = entry.getValue().get(0).getQuestion();
                        List<QuestionOption> selectedOptions = entry.getValue().stream()
                                .filter(resp -> resp.getSelectedOption() != null)
                                .map(StudentResponse::getSelectedOption)
                                .collect(Collectors.toList());
                        List<QuestionOption> correctOptions = questionOptionRepository.findByQuestionAndIsCorrect(question, true);
                        Set<Integer> selectedIds = selectedOptions.stream().map(QuestionOption::getOptionId).collect(Collectors.toSet());
                        Set<Integer> correctIds = correctOptions.stream().map(QuestionOption::getOptionId).collect(Collectors.toSet());
                        boolean isCorrect = question.getQuestionType() == QuestionType.MSQ
                                ? selectedIds.equals(correctIds)
                                : selectedOptions.size() == 1 && selectedOptions.get(0).getIsCorrect();
                        System.out.println("Question ID: " + question.getQuestionId() + ", Selected: " + selectedIds + ", Correct: " + correctIds + ", IsCorrect: " + isCorrect);
                        return isCorrect;
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet())
                    .size();

            topicWiseResults.add(TopicResultDTO.builder()
                    .topic(subtopicName)
                    .percentage(subtopicPercentage)
                    .passed(isPassed)
                    .totalQuestions(totalQuestions)
                    .correctQuestions(correctQuestions)
                    .build());
        }

        System.out.println("Total Marks: " + totalMarks);
        System.out.println("Obtained Marks: " + obtMarks);
        System.out.println("Total Questions by Topic: " + totalQuestionsByTopic);
        System.out.println("Correct Questions by Topic: " + topicWiseResults.stream()
                .collect(Collectors.toMap(TopicResultDTO::getTopic, TopicResultDTO::getCorrectQuestions)));

        return ExamResultTopicDTO.builder()
                .resultId(savedExamResult.getResultId())
                .percentage(percentage)
                .topics(topicWiseResults)
                .passed(passed)
                .totalMarks(totalMarks)
                .obtainedMarks(obtMarks)
                .remainingAttempts((int)(MAX_ATTEMPTS - studentAttempt.getAttemptCount()))
                .build();
    }

    public ExamResult getExamResult(Integer examId, Integer userId) {
        return examResultRepository.findByExam_ExamIdAndUser_UserId(examId, userId);
    }

    public ExamResultTopicDTO getExamResultById(Integer id) {
        ExamResult examResult = examResultRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Invalid ExamResult ID"));
        System.out.println("Fetching ExamResult: ID=" + id);

        // Find the attempt for this exam result
        StudentAttempt attempt = studentAttemptRepository.findByExamAndUserAndAttemptCount(
                examResult.getExam(),
                examResult.getUser(),
                studentAttemptRepository.countByUserAndExam(examResult.getUser(), examResult.getExam())
        ).orElseThrow(() -> new RuntimeException("Attempt not found for ExamResult ID: " + id));
        System.out.println("Attempt Found: ID=" + attempt.getAttemptId() + ", AttemptCount=" + attempt.getAttemptCount());

        int attempts = studentAttemptRepository.countByUserAndExam(examResult.getUser(), examResult.getExam());
        List<SubtopicResult> subtopicResults = subTopicResultRepository.findByExamResult(examResult);

        Map<String, Integer> totalQuestionsByTopic = new HashMap<>();
        Map<String, Integer> correctQuestionsByTopic = new HashMap<>();

        for (SubtopicResult subTopic : subtopicResults) {
            String subtopicName = subTopic.getSubtopic().getName();
            // Filter responses by the specific attempt
            List<StudentResponse> responses = studentResponseRepository
                    .findByAttemptAndQuestionSubtopic(attempt, subTopic.getSubtopic());
            System.out.println("Subtopic: " + subtopicName + ", Responses: " + responses.size());

            // Group responses by question
            Map<Integer, List<StudentResponse>> responsesByQuestion = responses.stream()
                    .collect(Collectors.groupingBy(resp -> resp.getQuestion().getQuestionId()));
            int totalQuestions = responsesByQuestion.size();
            totalQuestionsByTopic.put(subtopicName, totalQuestions);

            int correctQuestions = responsesByQuestion.entrySet().stream()
                    .filter(entry -> {
                        Question question = entry.getValue().get(0).getQuestion();
                        List<QuestionOption> selectedOptions = entry.getValue().stream()
                                .filter(resp -> resp.getSelectedOption() != null)
                                .map(StudentResponse::getSelectedOption)
                                .collect(Collectors.toList());
                        List<QuestionOption> correctOptions = questionOptionRepository.findByQuestionAndIsCorrect(question, true);
                        Set<Integer> selectedIds = selectedOptions.stream().map(QuestionOption::getOptionId).collect(Collectors.toSet());
                        Set<Integer> correctIds = correctOptions.stream().map(QuestionOption::getOptionId).collect(Collectors.toSet());
                        boolean isCorrect = question.getQuestionType() == QuestionType.MSQ
                                ? selectedIds.equals(correctIds)
                                : selectedOptions.size() == 1 && selectedOptions.get(0).getIsCorrect();
                        System.out.println("Question ID: " + question.getQuestionId() + ", Selected: " + selectedIds + ", Correct: " + correctIds + ", IsCorrect: " + isCorrect);
                        return isCorrect;
                    })
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toSet())
                    .size();
            correctQuestionsByTopic.put(subtopicName, correctQuestions);

            System.out.println("Topic: " + subtopicName);
            System.out.println("Total Questions: " + totalQuestions);
            System.out.println("Correct Questions: " + correctQuestions);
            System.out.println("===============================================");
        }

        List<TopicResultDTO> topicWiseResults = subtopicResults.stream()
                .map(subtopicResult -> {
                    String subtopicName = subtopicResult.getSubtopic().getName();
                    return TopicResultDTO.builder()
                            .topic(subtopicName)
                            .percentage(subtopicResult.getPercentage())
                            .passed(subtopicResult.getPassed())
                            .totalQuestions(totalQuestionsByTopic.getOrDefault(subtopicName, 0))
                            .correctQuestions(correctQuestionsByTopic.getOrDefault(subtopicName, 0))
                            .build();
                })
                .collect(Collectors.toList());

        return ExamResultTopicDTO.builder()
                .resultId(id)
                .topics(topicWiseResults)
                .percentage(examResult.getPercentage())
                .passed(examResult.getPassed())
                .totalMarks(examResult.getTotalMarks())
                .obtainedMarks(examResult.getObtainedMarks())
                .remainingAttempts(MAX_ATTEMPTS - attempts)
                .build();
    }

    public List<ExamResultResponseDTO> getAllExamResults() {
        return examResultRepository.findAll()
                .stream().map(examResult -> ExamResultResponseDTO
                        .builder()
                        .studentName(examResult.getUser().getName())
                        .examName(examResult.getExam().getTitle())
                        .totalMarks(examResult.getTotalMarks())
                        .percentage(examResult.getPercentage())
                        .isQualified(examResult.getPassed())
                        .obtainedMarks(examResult.getObtainedMarks())
                        .build())
                .toList();
    }

    public List<SubtopicDifficultyAnalysisDTO> getDifficultyLevelStatsByResultId(int resultId) {
        ExamResult result = examResultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("ExamResult not found"));

        StudentAttempt attempt = studentAttemptRepository
                .findTopByUserAndExamOrderByCreatedAtDesc(result.getUser(), result.getExam())
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        List<StudentResponse> responses = studentResponseRepository.findByAttempt(attempt);

        // Group by Question ID to avoid multiple counts
        Map<Integer, List<StudentResponse>> groupedByQuestion = responses.stream()
                .collect(Collectors.groupingBy(resp -> resp.getQuestion().getQuestionId()));

        Map<String, Map<String, DifficultyLevelStatsDTO>> analysis = new HashMap<>();

        for (List<StudentResponse> questionResponses : groupedByQuestion.values()) {
            Question question = questionResponses.get(0).getQuestion();
            if (question.getSubtopic() == null || question.getDifficultyLevel() == null) continue;

            String subtopic = question.getSubtopic().getName();
            String level = question.getDifficultyLevel().name();

            analysis.putIfAbsent(subtopic, new HashMap<>());
            var levelMap = analysis.get(subtopic);
            levelMap.putIfAbsent(level, new DifficultyLevelStatsDTO(level, 0, 0, 0, 0, 0));

            DifficultyLevelStatsDTO stats = levelMap.get(level);
            stats.setTotalQuestions(stats.getTotalQuestions() + 1);
            stats.setTotalMarks(stats.getTotalMarks() + question.getMarks());

            // Determine if correct
            boolean isCorrect = question.getQuestionType() == QuestionType.MSQ
                    ? isCorrectMSQ(questionResponses)
                    : questionResponses.stream().anyMatch(resp -> {
                        QuestionOption option = resp.getSelectedOption();
                        return option != null && option.getIsCorrect();
                    });

            if (isCorrect) {
                stats.setCorrect(stats.getCorrect() + 1);
                stats.setMarksObtained(stats.getMarksObtained() + question.getMarks());
            } else {
                stats.setIncorrect(stats.getIncorrect() + 1);
            }
        }

        // Convert map to list
        List<SubtopicDifficultyAnalysisDTO> resultList = new ArrayList<>();
        for (Map.Entry<String, Map<String, DifficultyLevelStatsDTO>> entry : analysis.entrySet()) {
            resultList.add(new SubtopicDifficultyAnalysisDTO(
                    entry.getKey(),
                    new ArrayList<>(entry.getValue().values())
            ));
        }

        return resultList;
    }
    private boolean isCorrectMSQ(List<StudentResponse> responses) {
        Question question = responses.get(0).getQuestion();

        Set<Integer> selectedIds = responses.stream()
                .map(StudentResponse::getSelectedOption)
                .filter(Objects::nonNull)
                .map(QuestionOption::getOptionId)
                .collect(Collectors.toSet());

        List<QuestionOption> correctOptions = questionOptionRepository.findByQuestionAndIsCorrect(question, true);
        Set<Integer> correctIds = correctOptions.stream().map(QuestionOption::getOptionId).collect(Collectors.toSet());

        return selectedIds.equals(correctIds);
    }
    public List<QuestionAnalysisDTO> getQuestionAnalysisByResultId(int resultId) {
        ExamResult result = examResultRepository.findById(resultId)
                .orElseThrow(() -> new RuntimeException("ExamResult not found"));

        StudentAttempt attempt = studentAttemptRepository
                .findTopByUserAndExamOrderByCreatedAtDesc(result.getUser(), result.getExam())
                .orElseThrow(() -> new RuntimeException("Attempt not found"));

        List<StudentResponse> responses = studentResponseRepository.findByAttempt(attempt);

        // Group responses by question
        Map<Integer, List<StudentResponse>> grouped = responses.stream()
                .collect(Collectors.groupingBy(resp -> resp.getQuestion().getQuestionId()));

        List<QuestionAnalysisDTO> analysisList = new ArrayList<>();

        for (Map.Entry<Integer, List<StudentResponse>> entry : grouped.entrySet()) {
            Question question = entry.getValue().get(0).getQuestion();

            List<String> correct = questionOptionRepository.findByQuestionAndIsCorrect(question, true)
                    .stream()
                    .map(QuestionOption::getOptionText)
                    .collect(Collectors.toList());

            List<String> selected = entry.getValue().stream()
                    .map(StudentResponse::getSelectedOption)
                    .filter(Objects::nonNull)
                    .map(QuestionOption::getOptionText)
                    .collect(Collectors.toList());

            analysisList.add(new QuestionAnalysisDTO(
                    question.getQuestionId(),
                    question.getQuestionText(),
                    question.getDifficultyLevel().name(),
                    correct,
                    selected
            ));
        }

        return analysisList;
    }


}
// @Service
// @RequiredArgsConstructor
// public class ExamResultService {

//         private final QuestionRepository questionRepository;
//         private final ExamResultRepository examResultRepository;
//         private final StudentResponseRepository studentResponseRepository;
//         private final StudentAttemptRepository studentAttemptRepository;
//         private final QuestionOptionRepository questionOptionRepository;
//         private final ExamRepository examRepository;
//         private final UserRepository userRepository;
//         private final SubtopicResultRepository subTopicResultRepository;
//         private final SubTopicService subtopicService;

//         public ExamResultTopicDTO calcExamRes(ExamResultRequest request) {
//                 String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();

//                 User user = userRepository.findByEmail(userEmail)
//                                 .orElseThrow(() -> new RuntimeException("Invalid User Email"));
//                 Exam exam = examRepository.findById(request.getExamId())
//                                 .orElseThrow(() -> new RuntimeException("Invalid Exam ID"));
                
//                 Integer attemptCount = studentAttemptRepository.countByUserAndExam(user,exam);
//                 StudentAttempt studentAttempt = new StudentAttempt();
//                 studentAttempt.setUser(user);
//                 studentAttempt.setExam(exam);
//                 studentAttempt.setAttemptCount(attemptCount +1);
//                 studentAttempt.setCreatedAt(LocalDateTime.now());
//                 studentAttempt = studentAttemptRepository.save(studentAttempt);

//                 List<StudentResponseRequest> responses = request.getResponses();

//                 // Total marks and questions by topic
//                 Map<String, Integer> totalMarksByTopic = new HashMap<>();
//                 Map<String, Integer> totalQuestionsByTopic = new HashMap<>();
//                 Map<String, Integer> correctQuestionsByTopic = new HashMap<>();

//                 responses.stream()
//                                 .map(resp -> questionRepository.findById(resp.getQuestion()).orElseThrow())
//                                 .forEach(quest -> {
//                                         String subtopicName = quest.getSubtopic().getName();
//                                         totalMarksByTopic.merge(subtopicName, quest.getMarks(), Integer::sum);
//                                         totalQuestionsByTopic.merge(subtopicName, 1, Integer::sum);
//                                 });

//                 // Total exam marks
//                 int totalMarks = responses.stream()
//                                 .map(resp -> questionRepository.findById(resp.getQuestion()).orElseThrow().getMarks())
//                                 .reduce(0, Integer::sum);

//                 Map<String, Double> resultList = new HashMap<>();

//                 for (StudentResponseRequest response : responses) {
//                         Question question = questionRepository.findById(response.getQuestion())
//                                         .orElseThrow(() -> new RuntimeException("Invalid Question ID"));

//                         List<QuestionOption> selectedOptions = questionOptionRepository
//                                         .findAllById(response.getSelectedOption());
                                        
//                         List<QuestionOption> correctOptions = questionOptionRepository
//                                         // .findByQuestionQuestionId(question.getQuestionId());
//                                         .findByQuestionAndIsCorrect(question, true);

//                         // If student doesn't submit any option due to violation
//                         if (selectedOptions.size() == 0) {
//                                 StudentResponse studentResponse = new StudentResponse();
//                                 // studentResponse.setUser(user);
//                                 studentResponse.setAttempt(studentAttempt);
//                                 studentResponse.setQuestion(question);
//                                 studentResponse.setSelectedOption(null);
//                                 studentResponse.setSubmittedAt(LocalDateTime.now());
//                                 studentResponseRepository.save(studentResponse);
//                         }

//                         // Save student responses
//                         for (QuestionOption selectedOption : selectedOptions) {
//                                 StudentResponse studentResponse = new StudentResponse();
//                                 // studentResponse.setUser(user);
//                                 studentResponse.setAttempt(studentAttempt);
//                                 studentResponse.setQuestion(question);
//                                 studentResponse.setSelectedOption(selectedOption);
//                                 studentResponse.setSubmittedAt(LocalDateTime.now());
//                                 studentResponseRepository.save(studentResponse);
//                         }

//                         String subtopicName = question.getSubtopic().getName();
//                         Set<Integer> selectedIds = selectedOptions.stream().map(QuestionOption::getOptionId)
//                                         .collect(Collectors.toSet());
//                         Set<Integer> correctIds = correctOptions.stream()
//                                         .filter(QuestionOption::getIsCorrect)
//                                         .map(QuestionOption::getOptionId)
//                                         .collect(Collectors.toSet());

//                         boolean isCorrect;

//                         if (question.getQuestionType() == QuestionType.MSQ) {
//                                 isCorrect = selectedIds.equals(correctIds); // Exact match required
//                         } else {
//                                 isCorrect = selectedOptions.size() == 1 && selectedOptions.get(0).getIsCorrect();
//                         }

//                         double obtainedMarks = isCorrect ? question.getMarks() : 0.0;
//                         resultList.put(subtopicName, resultList.getOrDefault(subtopicName, 0.0) + obtainedMarks);

//                         if (isCorrect) {
//                                 correctQuestionsByTopic.merge(subtopicName, 1, Integer::sum);
//                         }
//                 }

//                 double obtMarks = resultList.values().stream().reduce(0.0, Double::sum);
//                 double percentage = Math.round(((double) obtMarks / totalMarks) * 100);
//                 boolean passed = percentage >= 40;

//                 // Save exam result
//                 ExamResult result = ExamResult.builder()
//                                 .exam(exam)
//                                 .user(user)
//                                 .obtainedMarks(obtMarks)
//                                 .totalMarks(totalMarks)
//                                 .percentage(percentage)
//                                 .passed(passed)
//                                 .completedAt(LocalDateTime.now())
//                                 .build();

//                 ExamResult savedExamResult = examResultRepository.save(result);

//                 // Subtopic-wise result save
//                 List<TopicResultDTO> topicWiseResults = new ArrayList<>();

//                 for (String subtopicName : resultList.keySet()) {
//                         Double obtained = resultList.get(subtopicName);
//                         Integer total = totalMarksByTopic.get(subtopicName);
//                         double subtopicPercentage = (obtained / total) * 100;
//                         boolean isPassed = subtopicPercentage >= 40;

//                         SubtopicResult subtopicResult = SubtopicResult.builder()
//                                         .examResult(savedExamResult)
//                                         .subtopic(subtopicService.getSubtopicByName(subtopicName))
//                                         .totalMarks(total)
//                                         .percentage(subtopicPercentage)
//                                         .passed(isPassed)
//                                         .createdAt(LocalDateTime.now())
//                                         .build();

//                         subTopicResultRepository.save(subtopicResult);

//                         topicWiseResults.add(TopicResultDTO.builder()
//                                         .topic(subtopicName)
//                                         .percentage(subtopicPercentage)
//                                         .passed(isPassed)
//                                         .totalQuestions(totalQuestionsByTopic.getOrDefault(subtopicName, 0))
//                                         .correctQuestions(correctQuestionsByTopic.getOrDefault(subtopicName, 0))
//                                         .build());
//                 }

//                 return ExamResultTopicDTO.builder()
//                                 .resultId(savedExamResult.getResultId())
//                                 .percentage(percentage)
//                                 .topics(topicWiseResults)
//                                 .passed(passed)
//                                 .totalMarks(totalMarks)
//                                 .obtainedMarks(obtMarks)
//                                 .build();
//         }

//         public ExamResult getExamResult(Integer examId, Integer userId) {
//                 return examResultRepository.findByExam_ExamIdAndUser_UserId(examId, userId);
//         }

//         public ExamResultTopicDTO getExamResultById(Integer id) {
//                 ExamResult examResult = examResultRepository.findById(id)
//                                 .orElseThrow(() -> new RuntimeException("Invalid ExamResult ID"));

//                 int attempts = studentAttemptRepository.countByUserAndExam(examResult.getUser(), examResult.getExam());
//                 List<SubtopicResult> subtopicResults = subTopicResultRepository.findByExamResult(examResult);

//                 subtopicResults.stream().forEach(subTopic -> {
//                         System.out.println("Topic : " + subTopic.getSubtopic().getName());
//                         List<StudentResponse> responses = studentResponseRepository
//                                         .findByAttemptExamAndQuestionSubtopicAndAttemptUser(
//                                                         examResult.getExam(),
//                                                         subTopic.getSubtopic(),
//                                                         examResult.getUser());
//                         responses.stream().forEach(stuRes -> {
//                                 System.out.println("Question " + stuRes.getQuestion().getQuestionId() + " - " + stuRes.getQuestion().getQuestionText());
//                                 // System.out.println("Answer That's been selected : " + stuRes.getSelectedOption() != null
//                                 //                 ? stuRes.getSelectedOption().getOptionText()
//                                 //                 : "Option not selected");
//                         });
//                 });

//                 List<TopicResultDTO> topicWiseResults = subtopicResults.stream()
//                                 .map(subtopicResult -> {
//                                         String subtopicName = subtopicResult.getSubtopic().getName();
//                                         // Fetch total and correct questions for verification
//                                         // List<StudentResponse> responses = studentResponseRepository
//                                         // .findByQuestion_Subtopic_SubtopicIdAndUser_UserId(
//                                         // subtopicResult.getSubtopic().getSubtopicId(),
//                                         // examResult.getUser().getUserId());
//                                         List<StudentResponse> responses = studentResponseRepository
//                                                         .findByAttemptExamAndQuestionSubtopicAndAttemptUser(
//                                                                         examResult.getExam(),
//                                                                         subtopicResult.getSubtopic(),
//                                                                         examResult.getUser());

//                                         responses.stream().forEach(response -> {
//                                                 System.out.println("Question : "
//                                                                 + response.getQuestion().getQuestionText());
//                                         });

//                                         int totalQuestions = responses.stream()
//                                                         .map(StudentResponse::getQuestion)
//                                                         .collect(Collectors.toSet())
//                                                         .size();

//                                         var correctQuestions = responses.stream()
//                                                         .filter(resp -> {
//                                                                 Question question = resp.getQuestion();
//                                                                 List<QuestionOption> selectedOptions = resp
//                                                                                 .getSelectedOption() != null
//                                                                                                 ? List.of(resp.getSelectedOption())
//                                                                                                 : Collections.emptyList();

//                                                                 System.out.println(selectedOptions == null);

//                                                                 List<QuestionOption> correctOptions = questionOptionRepository
//                                                                                 // .findByQuestionQuestionId(question.getQuestionId());
//                                                                                 .findByQuestionAndIsCorrect(question,
//                                                                                                 true);
//                                                                 if (question.getQuestionType() == QuestionType.MSQ) {
//                                                                         Set<Integer> selectedIds = selectedOptions
//                                                                                         .stream()
//                                                                                         .map(QuestionOption::getOptionId)
//                                                                                         .collect(Collectors.toSet());
//                                                                         Set<Integer> correctIds = correctOptions
//                                                                                         .stream()
//                                                                                         .filter(QuestionOption::getIsCorrect)
//                                                                                         .map(QuestionOption::getOptionId)
//                                                                                         .collect(Collectors.toSet());
//                                                                         return selectedIds.equals(correctIds);
//                                                                 } else {
//                                                                         return selectedOptions.size() == 1
//                                                                                         && selectedOptions.get(0)
//                                                                                                         .getIsCorrect();
//                                                                 }
//                                                         })
//                                                         .collect(Collectors.groupingBy(
//                                                                         resp -> resp.getQuestion()
//                                                                                         .getQuestionId()))
//                                                         .size();

//                                         System.out.println("Total Questions : " + totalQuestions);
//                                         System.out.println("Correct Questions : " + correctQuestions);
//                                         System.out.println("Subtopic : " + subtopicName);

//                                         System.out.println("===============================================");

//                                         return TopicResultDTO.builder()
//                                                         .topic(subtopicName)
//                                                         .percentage(subtopicResult.getPercentage())
//                                                         .passed(subtopicResult.getPassed())
//                                                         .totalQuestions(totalQuestions)
//                                                         .correctQuestions(correctQuestions)
//                                                         .build();
//                                 })
//                                 .collect(Collectors.toList());

//                 return ExamResultTopicDTO.builder()
//                                 .resultId(id)
//                                 .topics(topicWiseResults)
//                                 .percentage(examResult.getPercentage())
//                                 .passed(examResult.getPassed())
//                                 .totalMarks(examResult.getTotalMarks())
//                                 .obtainedMarks(examResult.getObtainedMarks())
//                                 .remainingAttempts(attempts)
//                                 .build();
//         }

//         public List<ExamResultResponseDTO> getAllExamResults() {
//                 return examResultRepository.findAll()
//                                 .stream().map(examResult -> ExamResultResponseDTO
//                                                 .builder()
//                                                 .studentName(examResult.getUser().getName())
//                                                 .examName(examResult.getExam().getTitle())
//                                                 .totalMarks(examResult.getTotalMarks())
//                                                 .percentage(examResult.getPercentage())
//                                                 .isQualified(examResult.getPassed())
//                                                 .obtainedMarks(examResult.getObtainedMarks())
//                                                 .build())
//                                 .toList();
//         }
// }
// public ExamResultTopicDTO calcExamRes(ExamResultRequest request) {
// String userEmail =
// SecurityContextHolder.getContext().getAuthentication().getName();

// User user = userRepository.findByEmail(userEmail)
// .orElseThrow(() -> new RuntimeException("Invalid User Email"));
// Exam exam = examRepository.findById(request.getExamId())
// .orElseThrow(() -> new RuntimeException("Invalid Exam ID"));

// List<StudentResponseRequest> responses = request.getResponses();

// // Total marks and questions by topic
// Map<String, Integer> totalMarksByTopic = new HashMap<>();
// Map<String, Integer> totalQuestionsByTopic = new HashMap<>();
// Map<String, Integer> correctQuestionsByTopic = new HashMap<>();

// responses.stream()
// .map(resp -> questionRepository.findById(resp.getQuestion()).orElseThrow())
// .forEach(quest -> {
// String subtopicName = quest.getSubtopic().getName();
// totalMarksByTopic.merge(subtopicName, quest.getMarks(), Integer::sum);
// totalQuestionsByTopic.merge(subtopicName, 1, Integer::sum);
// });

// // Total exam marks
// int totalMarks = responses.stream()
// .map(resp ->
// questionRepository.findById(resp.getQuestion()).orElseThrow().getMarks())
// .reduce(0, Integer::sum);

// Map<String, Double> resultList = new HashMap<>();

// for (StudentResponseRequest response : responses) {
// Question question = questionRepository.findById(response.getQuestion())
// .orElseThrow(() -> new RuntimeException("Invalid Question ID"));

// List<QuestionOption> selectedOptions =
// questionOptionRepository.findAllById(response.getSelectedOption());
// List<QuestionOption> correctOptions = questionOptionRepository
// .findByQuestionQuestionId(question.getQuestionId());

// // Save student responses
// for (QuestionOption selectedOption : selectedOptions) {
// StudentResponse studentResponse = new StudentResponse();
// studentResponse.setUser(user);
// studentResponse.setQuestion(question);
// studentResponse.setSelectedOption(selectedOption);
// studentResponse.setSubmittedAt(LocalDateTime.now());
// studentResponseRepository.save(studentResponse);
// }

// String subtopicName = question.getSubtopic().getName();
// Set<Integer> selectedIds =
// selectedOptions.stream().map(QuestionOption::getOptionId)
// .collect(Collectors.toSet());
// Set<Integer> correctIds = correctOptions.stream()
// .filter(QuestionOption::getIsCorrect)
// .map(QuestionOption::getOptionId)
// .collect(Collectors.toSet());

// boolean isCorrect;

// if (question.getQuestionType() == QuestionType.MSQ) {
// isCorrect = selectedIds.equals(correctIds); // Exact match required
// } else {
// isCorrect = selectedOptions.size() == 1 &&
// selectedOptions.get(0).getIsCorrect();
// }

// double obtainedMarks = isCorrect ? question.getMarks() : 0.0;
// resultList.put(subtopicName, resultList.getOrDefault(subtopicName, 0.0) +
// obtainedMarks);

// if (isCorrect) {
// correctQuestionsByTopic.merge(subtopicName, 1, Integer::sum);
// }
// }

// double obtMarks = resultList.values().stream().reduce(0.0, Double::sum);
// double percentage = Math.round(((double) obtMarks / totalMarks) * 100);
// boolean passed = percentage >= 40;

// // Save exam result
// ExamResult result = ExamResult.builder()
// .exam(exam)
// .user(user)
// .obtainedMarks(obtMarks)
// .totalMarks(totalMarks)
// .percentage(percentage)
// .passed(passed)
// .completedAt(LocalDateTime.now())
// .build();

// ExamResult savedExamResult = examResultRepository.save(result);

// // Subtopic-wise result save
// List<TopicResultDTO> topicWiseResults = new ArrayList<>();

// for (String subtopicName : resultList.keySet()) {
// Double obtained = resultList.get(subtopicName);
// Integer total = totalMarksByTopic.get(subtopicName);
// double subtopicPercentage = (obtained / total) * 100;
// boolean isPassed = subtopicPercentage >= 40;

// SubtopicResult subtopicResult = SubtopicResult.builder()
// .examResult(savedExamResult)
// .subtopic(subtopicService.getSubtopicByName(subtopicName))
// .totalMarks(total)
// .percentage(subtopicPercentage)
// .passed(isPassed)
// .createdAt(LocalDateTime.now())
// .build();

// subTopicResultRepository.save(subtopicResult);

// topicWiseResults.add(TopicResultDTO.builder()
// .topic(subtopicName)
// .percentage(subtopicPercentage)
// .passed(isPassed)
// .totalQuestions(totalQuestionsByTopic.getOrDefault(subtopicName, 0))
// .correctQuestions(correctQuestionsByTopic.getOrDefault(subtopicName, 0))
// .build());
// }

// return ExamResultTopicDTO.builder()
// .resultId(savedExamResult.getResultId())
// .percentage(percentage)
// .topics(topicWiseResults)
// .passed(passed)
// .totalMarks(totalMarks)
// .obtainedMarks(obtMarks)
// .build();
// }

// public ExamResultTopicDTO calcExamRes(ExamResultRequest request) {
// String userEmail =
// SecurityContextHolder.getContext().getAuthentication().getName();
//
// Student stu = studentRepository.findByEmail(userEmail)
// .orElseThrow(() -> new RuntimeException("Invalid Student Mail"));
// Exam exam = examRepository.findById(request.getExamId())
// .orElseThrow(() -> new RuntimeException("Invalid Exam ID"));
//
// List<StudentResponseRequest> responses = request.getResponses();
//
// // 2nd type: marks
// Map<String, Integer> totalMarksByTopic = new HashMap<>();
//
// // Processing total marks by topic
// responses.stream().map(resp -> resp.getQuestion()) // Get question ids from
// responses
// .map(questId -> questionRepository.findById(questId).get()) // Fetch the
// Question entity
// .forEach(quest -> { // For each Question entity, process
// SubTopic topic = quest.getSubtopic(); // Get the SubTopic associated with the
// question
// if(totalMarksByTopic.containsKey(topic.getName())) {
// int marks = totalMarksByTopic.get(topic.getName());
// totalMarksByTopic.put(topic.getName(), marks + quest.getMarks());
// } else {
// totalMarksByTopic.put(topic.getName(), quest.getMarks());
// }
// });
//
// System.out.println(totalMarksByTopic);
//
// int totalMarks = responses.stream().map(resp -> resp.getQuestion())
// .map(questId -> questionRepository.findById(questId).get().getMarks())
// .reduce(0, (a, b) -> a + b);
//
// System.out.println("Total Marks: " + totalMarks);
//
// // 2nd value: Marks
// Map<String, Double> resultList = new HashMap<String, Double>(); // Result
// based upon topic
//
// for (StudentResponseRequest response : responses) {
// List<QuestionOption> selectedOptions =
// questionOptionRepository.findAllById(response.getSelectedOption());
// for (QuestionOption option : selectedOptions) {
// String subtopic = option.getQuestion().getSubtopic().getName();
// if (resultList.containsKey(subtopic)) {
// if (option.getIsCorrect()) {
// if (option.getQuestion().getQuestionType() == QuestionType.MSQ) {
// int count =
// questionRepository.countCorrectOptionsByQuestionId(option.getQuestion().getQuestionId());
// double marks = resultList.get(subtopic) + (option.getQuestion().getMarks() /
// (double)count);
// resultList.put(subtopic, marks);
// } else {
// double marks = resultList.get(subtopic) + option.getQuestion().getMarks();
// resultList.put(subtopic, marks);
// }
// }
// } else {
// resultList.put(subtopic, option.getIsCorrect() ?
// option.getQuestion().getMarks() : 0.0);
// }
// }
// }
//
// System.out.println(resultList);
//
// List<TopicResultDTO> topicWiseResults = new ArrayList<TopicResultDTO>();
//// ExamResult examResult =
// examResultRepository.findByExam_ExamIdAndStudent_StudentId(exam.getExamId(),
// stu.getStudentId());
// Double obtMarks = resultList.values().stream()
// .reduce(0.0, (a, b) -> a + b);
//
// double percentage = ((double) obtMarks /(double) totalMarks) * 100;
// boolean passed = percentage >= 40;
// System.out.println("Total marks : " + totalMarks + " , obt marks : " +
// obtMarks);
// System.out.println("Final Total percentage : " + percentage + " Passed : " +
// passed);
//
// ExamResult result = ExamResult.builder()
// .exam(exam)
// .student(stu)
// .obtainedMarks(obtMarks)
// .totalMarks(totalMarks)
// .percentage(percentage)
// .passed(passed)
// .completedAt(LocalDateTime.now())
// .build();
// ExamResult savedExamResult = examResultRepository.save(result);
//
// for (String key : resultList.keySet()) {
//
// Double obtResult = resultList.get(key);
// Integer totResult = totalMarksByTopic.get(key);
// double subtopicPercentage = ((obtResult) / (double)totResult) * 100;
// boolean isPassed = subtopicPercentage >= 40;
//
// SubtopicResult subtopicResult = SubtopicResult.builder()
// .examResult(savedExamResult)
// .subtopic(subtopicService.getSubtopicByName(key))
// .totalMarks(totResult)
// .percentage(subtopicPercentage)
// .passed(isPassed)
// .createdAt(LocalDateTime.now())
// .build();
//
// subTopicResultRepository.save(subtopicResult);
// TopicResultDTO dto = TopicResultDTO.builder()
// .topic(key)
// .percentage(subtopicPercentage)
// .passed(isPassed)
// .build();
//
// topicWiseResults.add(dto);
//
// System.out.println("Subtopic " + key + " , perc % : " + percentage + " ,
// Passed : " + passed);
// }
//
// ExamResultTopicDTO dto = ExamResultTopicDTO.builder()
// .resultId(savedExamResult.getResultId())
// .percentage(percentage)
// .topics(topicWiseResults)
// .passed(passed)
// .totalMarks(totalMarks)
// .obtainedMarks(obtMarks)
// .build();
//
// return dto;
// }
