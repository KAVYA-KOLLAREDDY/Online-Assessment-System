package com.examApplication.examApplication.service;

import com.examApplication.examApplication.dto.ExamQuestionsResponseDTO;
import com.examApplication.examApplication.dto.ExamUploadDTO;
import com.examApplication.examApplication.dto.GenerateQuestionsDTO;
import com.examApplication.examApplication.dto.SubtopicDifficultyRequest;
import com.examApplication.examApplication.dto.SubtopicDistributionDTO;
import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.ExamSubtopicDistribution;
import com.examApplication.examApplication.entity.Question;
import com.examApplication.examApplication.entity.QuestionOption;
import com.examApplication.examApplication.entity.SubTopic;
import com.examApplication.examApplication.entity.Subject;
import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.model.DifficultyLevel;
import com.examApplication.examApplication.model.ExamStatus;
import com.examApplication.examApplication.model.ExamType;
import com.examApplication.examApplication.model.QuestionType;
import com.examApplication.examApplication.repository.ExamRepository;
import com.examApplication.examApplication.repository.ExamSubtopicDistributionRepository;
import com.examApplication.examApplication.repository.QuestionOptionRepository;
import com.examApplication.examApplication.repository.QuestionRepository;
import com.examApplication.examApplication.repository.SubTopicRepository;
import com.examApplication.examApplication.repository.SubjectRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final ExamRepository examRepository;
    private final QuestionOptionRepository quOptRepo;
    private final SubTopicRepository subtopicRepo;
    private final SubjectRepository subjectRepository;
    private final ExamSubtopicDistributionRepository distributionRepository;
    private final AuthService authService;

    public boolean hasExamDistributions(int examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));
        return !distributionRepository.findByExam(exam).isEmpty();
    }

    public ExamQuestionsResponseDTO getQuestionsByExam(int examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        List<Question> allQuestions = questionRepository.findByExamExamId(examId);
        if (allQuestions.isEmpty()) {
            throw new RuntimeException("No questions found for the given exam.");
        }

        List<ExamSubtopicDistribution> distributions = distributionRepository.findByExam(exam);
        if (distributions.isEmpty()) {
            throw new RuntimeException("No distribution data found for the given exam.");
        }

        Map<Long, List<Question>> questionsBySubtopic = allQuestions.stream()
                .filter(q -> q.getSubtopic() != null)
                .collect(Collectors.groupingBy(q -> q.getSubtopic().getSubtopicId()));

        List<Question> finalQuestions = new ArrayList<>();
        Integer totalQuestionsRequired = exam.getTotalQuestions();
        if (totalQuestionsRequired == null || totalQuestionsRequired <= 0) {
            throw new RuntimeException("Total questions not defined for this exam.");
        }

        for (ExamSubtopicDistribution dist : distributions) {
            Long subtopicId = dist.getSubtopic().getSubtopicId();
            int subtopicQuestionCount = (int) Math.round((dist.getSubtopicPercentage() / 100f) * totalQuestionsRequired);

            List<Question> subtopicQuestions = questionsBySubtopic.getOrDefault(subtopicId, new ArrayList<>());
            if (subtopicQuestions.isEmpty()) continue;

            Map<DifficultyLevel, List<Question>> questionsByDifficulty = subtopicQuestions.stream()
                    .filter(q -> q.getDifficultyLevel() != null)
                    .collect(Collectors.groupingBy(Question::getDifficultyLevel));

            int basicCount = (int) Math.round((dist.getBasicPercentage() / 100f) * subtopicQuestionCount);
            int intermediateCount = (int) Math.round((dist.getIntermediatePercentage() / 100f) * subtopicQuestionCount);
            int advanceCount = subtopicQuestionCount - basicCount - intermediateCount;

            finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.get(DifficultyLevel.Basic), basicCount));
            finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.get(DifficultyLevel.Intermediate), intermediateCount));
            finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.get(DifficultyLevel.Advance), advanceCount));
        }

        if (finalQuestions.size() < totalQuestionsRequired) {
            List<Question> remainingPool = allQuestions.stream()
                    .filter(q -> !finalQuestions.contains(q))
                    .collect(Collectors.toList());
            Collections.shuffle(remainingPool);
            finalQuestions.addAll(remainingPool.subList(0, Math.min(totalQuestionsRequired - finalQuestions.size(), remainingPool.size())));
        }

        finalQuestions.forEach(q -> {
            if (q.getOptions() != null) Collections.shuffle(q.getOptions());
        });
        Collections.shuffle(finalQuestions);

        if (finalQuestions.size() > totalQuestionsRequired) {
            finalQuestions.subList(totalQuestionsRequired, finalQuestions.size()).clear();
            log.info("Trimmed final questions to total required: {}", finalQuestions.size());
        }

        // Calculate actual subtopic counts from finalQuestions
        Map<String, Integer> subtopicQuestionCounts = getSubtopicCounts(finalQuestions);

        return new ExamQuestionsResponseDTO(finalQuestions, subtopicQuestionCounts);
    }

    private Map<String, Integer> getSubtopicCounts(List<Question> questions) {
        return questions.stream()
                .filter(q -> q.getSubtopic() != null)
                .collect(Collectors.groupingBy(
                    q -> q.getSubtopic().getName(),
                    Collectors.summingInt(q -> 1)
                ));
    }

    public List<Question> generateCustomQuestion(GenerateQuestionsDTO request) {
        Exam exam = examRepository.findById(request.getExamId())
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        List<Question> allQuestions = questionRepository.findByExamExamId(request.getExamId());
        if (allQuestions.isEmpty()) {
            throw new RuntimeException("No questions found for the given exam.");
        }

        exam.setTotalQuestions(request.getTotalQuestions());
        if (request.getExamType() != null) {
            try {
                exam.setExamType(ExamType.valueOf(request.getExamType()));
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException("Invalid ExamType: " + request.getExamType());
            }
        }

        examRepository.save(exam);

        double totalSubtopicPercent = request.getDistribution().stream()
                .mapToDouble(SubtopicDifficultyRequest::getPercentage)
                .sum();
        if (totalSubtopicPercent > 100) {
            throw new RuntimeException("Total subtopic percentage exceeds 100%. Found: " + totalSubtopicPercent + "%");
        }

        for (SubtopicDifficultyRequest dist : request.getDistribution()) {
            if (dist.getSubtopicId() == null) {
                throw new RuntimeException("Subtopic ID in distribution cannot be null");
            }

            SubTopic subtopic = subtopicRepo.findById(dist.getSubtopicId().longValue())
                    .orElseThrow(() -> new RuntimeException("Subtopic not found for ID: " + dist.getSubtopicId()));

            ExamSubtopicDistribution examDistribution = new ExamSubtopicDistribution();
            examDistribution.setExam(exam);
            examDistribution.setSubtopic(subtopic);
            examDistribution.setSubtopicPercentage(dist.getPercentage());

            var dp = dist.getDifficultyDistribution();
            examDistribution.setBasicPercentage(dp.getBasic());
            examDistribution.setIntermediatePercentage(dp.getIntermediate());
            examDistribution.setAdvancePercentage(dp.getAdvance());

            distributionRepository.save(examDistribution);
        }

        Map<Long, List<Question>> questionsBySubtopic = allQuestions.stream()
                .filter(q -> q.getSubtopic() != null)
                .collect(Collectors.groupingBy(q -> q.getSubtopic().getSubtopicId()));

        List<Question> finalQuestions = new ArrayList<>();
        int totalQuestionsRequired = request.getTotalQuestions();

        for (SubtopicDifficultyRequest dist : request.getDistribution()) {
            if (dist.getSubtopicId() == null) continue;

            Long subtopicId = dist.getSubtopicId().longValue();
            int subtopicQuestionCount = (int) Math.round((dist.getPercentage() / 100f) * totalQuestionsRequired);

            List<Question> subtopicQuestions = questionsBySubtopic.getOrDefault(subtopicId, new ArrayList<>());
            if (subtopicQuestions.isEmpty()) continue;

            var dp = dist.getDifficultyDistribution();
            Map<DifficultyLevel, List<Question>> questionsByDifficulty = subtopicQuestions.stream()
                    .filter(q -> q.getDifficultyLevel() != null)
                    .collect(Collectors.groupingBy(Question::getDifficultyLevel));

            int basicCount = (int) Math.round((dp.getBasic() / 100f) * subtopicQuestionCount);
            int intermediateCount = (int) Math.round((dp.getIntermediate() / 100f) * subtopicQuestionCount);
            int advanceCount = subtopicQuestionCount - basicCount - intermediateCount;

            finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.get(DifficultyLevel.Basic), basicCount));
            finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.get(DifficultyLevel.Intermediate), intermediateCount));
            finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.get(DifficultyLevel.Advance), advanceCount));
        }

        if (finalQuestions.size() < totalQuestionsRequired) {
            List<Question> remainingPool = allQuestions.stream()
                    .filter(q -> !finalQuestions.contains(q))
                    .collect(Collectors.toList());
            Collections.shuffle(remainingPool);
            finalQuestions.addAll(remainingPool.subList(0, Math.min(totalQuestionsRequired - finalQuestions.size(), remainingPool.size())));
        }

        finalQuestions.forEach(q -> {
            if (q.getOptions() != null) Collections.shuffle(q.getOptions());
        });
        Collections.shuffle(finalQuestions);

        return finalQuestions.size() > totalQuestionsRequired ? finalQuestions.subList(0, totalQuestionsRequired) : finalQuestions;
    }

    private List<Question> pickRandomQuestions(List<Question> questions, int count) {
        if (questions == null || questions.isEmpty()) return new ArrayList<>();
        Collections.shuffle(questions);
        return questions.subList(0, Math.min(count, questions.size()));
    }

    public List<SubtopicDistributionDTO> getExamDistributions(int examId) {
    Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new RuntimeException("Exam not found"));

    List<ExamSubtopicDistribution> distributions = distributionRepository.findByExam(exam);
    if (distributions.isEmpty()) {
        throw new RuntimeException("No distribution data found for the given exam.");
    }

    // Convert to DTO
    return distributions.stream().map(dist -> new SubtopicDistributionDTO(
            dist.getId(),
            dist.getSubtopic().getSubtopicId(),
            dist.getSubtopic().getName(),
            dist.getSubtopicPercentage(),
            dist.getBasicPercentage(),
            dist.getIntermediatePercentage(),
            dist.getAdvancePercentage()
    )).toList();
}

    @Transactional
    public List<ExamSubtopicDistribution> updateSubtopicPercentages(GenerateQuestionsDTO request) {
    List<ExamSubtopicDistribution> updatedList = new ArrayList<>();

    Exam exam = examRepository.findById(request.getExamId())
            .orElseThrow(() -> new RuntimeException("Exam not found"));

    double totalSubtopicPercentage = request.getDistribution().stream()
            .mapToDouble(SubtopicDifficultyRequest::getPercentage)
            .sum();
    if (totalSubtopicPercentage > 100) {
        throw new RuntimeException("Total subtopic percentage exceeds 100%. Found: " + totalSubtopicPercentage + "%");
    }

    for (SubtopicDifficultyRequest dist : request.getDistribution()) {
        Integer subtopicId = dist.getSubtopicId();
        if (subtopicId == null) {
            throw new RuntimeException("Subtopic ID in distribution cannot be null");
        }

        ExamSubtopicDistribution existingDistribution =
                distributionRepository.findByExamAndSubtopic_SubtopicId(exam, subtopicId)
                        .orElseThrow(() -> new RuntimeException("Distribution not found for subtopic ID: " + subtopicId));

        existingDistribution.setSubtopicPercentage(dist.getPercentage());
        SubtopicDifficultyRequest.DifficultyPercentages dp = dist.getDifficultyDistribution();
        existingDistribution.setBasicPercentage(dp.getBasic());
        existingDistribution.setIntermediatePercentage(dp.getIntermediate());
        existingDistribution.setAdvancePercentage(dp.getAdvance());

        updatedList.add(distributionRepository.save(existingDistribution));
    }

    return updatedList;
}

    public void uploadSubTopicsWithQuestions(ExamUploadDTO dto) {
        try (XSSFWorkbook workbook = new XSSFWorkbook(dto.getFile().getInputStream())) {

            Subject subject = subjectRepository.findById(dto.getSubjectId())
                    .orElseThrow(() -> new RuntimeException("Subject not found"));
            Sheet sheet = workbook.getSheetAt(0);
        
            User currentUser = authService.getUser();

            Exam exam = new Exam();
            exam.setTitle(dto.getTitle());
            exam.setDescription(dto.getDescription());
            exam.setStartTime(dto.getStartTime());
            exam.setEndTime(dto.getEndTime());
            exam.setDuration(dto.getDuration());
            exam.setSubject(subject);
            exam.setCreatedBy(currentUser);

            // ✅ Set exam status based on date logic (inline)
            LocalDate today = LocalDate.now();
            if (today.isBefore(dto.getStartTime())) {
                exam.setExamStatus(ExamStatus.SCHEDULED);
            } else if (today.isAfter(dto.getEndTime())) {
                exam.setExamStatus(ExamStatus.COMPLETED);
            } else {
                exam.setExamStatus(ExamStatus.ONGOING);
            }
 
            System.out.println("DTO startTime: " + dto.getStartTime());
            System.out.println("DTO endTime: " + dto.getEndTime());

            Exam savedExam = examRepository.save(exam);

            for (Row row : sheet) {
                if (row.getRowNum() == 0)
                    continue;

                Cell subtopicCell = row.getCell(0);
                if (subtopicCell == null || subtopicCell.getCellType() == CellType.BLANK) {
                    System.out.println("Subtopic ID is missing at row " + row.getRowNum());
                    continue;
                }
                long subtopicId = (long) subtopicCell.getNumericCellValue();

                if (subtopicId <= 0) {
                    System.out.println("Invalid Subtopic ID: " + subtopicId);
                    continue; // Skip this row if invalid ID
                }

                SubTopic topic = subtopicRepo.findById(subtopicId)
                        .orElseThrow(() -> new RuntimeException("Invalid Subtopic id" + subtopicId));

                boolean exists = subtopicRepo.existsByNameAndSubject(topic.getName(), subject);
                if (!exists) {
                    throw new RuntimeException("SubTopic not found");
                }

                String questionText = row.getCell(1).getStringCellValue();
                String qType = row.getCell(3).getStringCellValue();
                String difficultyLevel = row.getCell(4).getStringCellValue();
                int marks = (int) row.getCell(row.getLastCellNum() - 1).getNumericCellValue();

                String correctOptionLetter = row.getCell(row.getLastCellNum() - 2).getStringCellValue();
                List<String> correctOptions = parseCorrectOptions(correctOptionLetter);
                System.out.println("Correct Options : " + correctOptions);

                List<String> options = new ArrayList<>();
                for (int i = 5; i < row.getLastCellNum() - 2; i++) {
                    Cell cell = row.getCell(i);
                    if (cell == null || cell.getCellType() == CellType.BLANK)
                        break;
                    String value = "";
                    switch (cell.getCellType()) {
                        case STRING:
                            value = cell.getStringCellValue().trim();
                            break;
                        case NUMERIC:
                            value = String.valueOf(cell.getNumericCellValue()).trim();
                            break;
                        case BOOLEAN:
                            value = String.valueOf(cell.getBooleanCellValue()).trim();
                            break;
                        default:
                            value = "";
                    }

                    options.add(value);
                }

                System.out.println("Options : " + options);

                Set<Integer> correctIndexes = Arrays.stream(correctOptionLetter.split(","))
                        .map(String::trim)
                        .map(letter -> letter.charAt(0) - 'A')
                        .filter(index -> index >= 0 && index < options.size())
                        .collect(Collectors.toSet());

                if (correctIndexes.isEmpty()) {
                    throw new RuntimeException("No valid correct options provided for: " + questionText);
                }

                Question question = new Question();
                question.setQuestionText(questionText);
                question.setQuestionType(QuestionType.valueOf(qType));
                question.setDifficultyLevel(DifficultyLevel.valueOf(difficultyLevel));
                question.setMarks(marks);
                question.setSubtopic(topic);
                question.setExam(exam);

                System.out.println("Options Size : " + options.size());
                for (int i = 0; i < options.size(); i++) {
                    QuestionOption option = new QuestionOption();
                    option.setOptionText(options.get(i));
                    option.setIsCorrect(correctIndexes.contains(i));
                    question.addQuestionOption(option);
                }

                savedExam.addQuestion(question);
            }

            examRepository.save(savedExam);

        } catch (IOException e) {
            throw new RuntimeException("Error processing file " + e.getMessage());
        } catch (IllegalStateException ie) {
            throw new RuntimeException("Please upload xl by checking the data format!");
        }
    }

    public static List<String> parseCorrectOptions(String correctOptionInput) {
        correctOptionInput = correctOptionInput.trim();

        if (!isValidInput(correctOptionInput)) {
            throw new IllegalArgumentException("Invalid characters in input! Only letters A-E and commas are allowed.");
        }

        StringTokenizer tokenizer = new StringTokenizer(correctOptionInput, ",");
        List<String> options = new ArrayList<>();

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim().toUpperCase();
            if (!isValidOption(token)) {
                throw new IllegalArgumentException(
                        "Invalid option detected: " + token + ". Only options A-E are allowed.");
            }
            options.add(token);
        }

        return options;
    }

    private static boolean isValidInput(String input) {
        String regex = "^[A-Ea-e, ]+$";
        return Pattern.matches(regex, input);
    }

    private static boolean isValidOption(String option) {
        return option.matches("[A-E]");
    }

    public List<Question> getAllQuestionsByExam(int examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Questions not found"));
        return questionRepository.findByExam(exam);
    }

     public Map<String, Integer> getSubtopicQuestionCounts(int examId) {
        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        Integer totalQuestions = exam.getTotalQuestions();
        if (totalQuestions == null || totalQuestions <= 0) {
            throw new RuntimeException("Total questions not defined for this exam.");
        }

        List<ExamSubtopicDistribution> distributions = distributionRepository.findByExam(exam);
        if (distributions.isEmpty()) {
            throw new RuntimeException("No distribution data found for the given exam.");
        }

        Map<String, Integer> subtopicQuestionCounts = new HashMap<>();
        int calculatedTotal = 0;

        // Calculate questions per subtopic
        for (ExamSubtopicDistribution dist : distributions) {
            String subtopicName = dist.getSubtopic().getName();
            double percentage = dist.getSubtopicPercentage();
            int questionCount = (int) Math.round((percentage / 100f) * totalQuestions);
            subtopicQuestionCounts.put(subtopicName, questionCount);
            calculatedTotal += questionCount;
        }

        // Adjust for rounding discrepancies
        if (calculatedTotal != totalQuestions) {
            int difference = totalQuestions - calculatedTotal;
            // Find subtopic with highest percentage to adjust
            ExamSubtopicDistribution maxDist = distributions.stream()
                    .max(Comparator.comparingDouble(ExamSubtopicDistribution::getSubtopicPercentage))
                    .orElse(distributions.get(0));
            String maxSubtopicName = maxDist.getSubtopic().getName();
            subtopicQuestionCounts.compute(maxSubtopicName, (k, v) -> v + difference);
        }

        return subtopicQuestionCounts;
    }
}

    // public List<Question> getQuestionsByExam(int examId) {
    //     Exam exam = examRepository.findById(examId)
    //             .orElseThrow(() -> new RuntimeException("Exam not found"));

    //     List<Question> allQuestions = questionRepository.findByExamExamId(examId);
    //     if (allQuestions.isEmpty()) {
    //         throw new RuntimeException("No questions found for the given exam.");
    //     }

    //     List<ExamSubtopicDistribution> distributions = distributionRepository.findByExam(exam);
    //     if (distributions.isEmpty()) {
    //         throw new RuntimeException("No distribution data found for the given exam.");
    //     }

    //     Map<Long, List<Question>> questionsBySubtopic = allQuestions.stream()
    //             .filter(q -> q.getSubtopic() != null)
    //             .collect(Collectors.groupingBy(q -> q.getSubtopic().getSubtopicId()));

    //     List<Question> finalQuestions = new ArrayList<>();
    //     Integer totalQuestionsRequired = exam.getTotalQuestions();
    //     if (totalQuestionsRequired == null || totalQuestionsRequired <= 0) {
    //         throw new RuntimeException("Total questions not defined for this exam.");
    //     }

    //     for (ExamSubtopicDistribution dist : distributions) {
    //         Long subtopicId = dist.getSubtopic().getSubtopicId();
    //         int subtopicQuestionCount = (int) Math.round((dist.getSubtopicPercentage() / 100f) * totalQuestionsRequired);

    //         List<Question> subtopicQuestions = questionsBySubtopic.getOrDefault(subtopicId, new ArrayList<>());
    //         if (subtopicQuestions.isEmpty()) continue;

    //         Map<DifficultyLevel, List<Question>> questionsByDifficulty = subtopicQuestions.stream()
    //                 .filter(q -> q.getDifficultyLevel() != null)
    //                 .collect(Collectors.groupingBy(Question::getDifficultyLevel));

    //         int basicCount = (int) Math.round((dist.getBasicPercentage() / 100f) * subtopicQuestionCount);
    //         int intermediateCount = (int) Math.round((dist.getIntermediatePercentage() / 100f) * subtopicQuestionCount);
    //         int advanceCount = subtopicQuestionCount - basicCount - intermediateCount;

    //         finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.get(DifficultyLevel.Basic), basicCount));
    //         finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.get(DifficultyLevel.Intermediate), intermediateCount));
    //         finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.get(DifficultyLevel.Advance), advanceCount));
    //     }

    //     if (finalQuestions.size() < totalQuestionsRequired) {
    //         List<Question> remainingPool = allQuestions.stream()
    //                 .filter(q -> !finalQuestions.contains(q))
    //                 .collect(Collectors.toList());
    //         Collections.shuffle(remainingPool);
    //         finalQuestions.addAll(remainingPool.subList(0, Math.min(totalQuestionsRequired - finalQuestions.size(), remainingPool.size())));
    //     }

    //     finalQuestions.forEach(q -> {
    //         if (q.getOptions() != null) Collections.shuffle(q.getOptions());
    //     });
    //     Collections.shuffle(finalQuestions);

    //     if (finalQuestions.size() > totalQuestionsRequired) {
    //         finalQuestions.subList(totalQuestionsRequired, finalQuestions.size()).clear();
    //         System.out.println("Trimmed final questions to total required: " + finalQuestions.size());
    //     }

    //     return finalQuestions;
    // }

// @Service
// @RequiredArgsConstructor
// @Slf4j
// public class QuestionService {

//     private final QuestionRepository questionRepository;
//     private final ExamRepository examRepository;
//     private final QuestionOptionRepository quOptRepo;
//     private final SubTopicRepository subtopicRepo;
//     private final SubjectRepository subjectRepository;
//     private final ExamSubtopicDistributionRepository distributionRepository;

//  public List<Question> getQuestionsByExam(int examId) {
//     Exam exam = examRepository.findById(examId)
//             .orElseThrow(() -> new RuntimeException("Exam not found"));

//     List<Question> allQuestions = questionRepository.findByExamExamId(examId);
//     if (allQuestions.isEmpty()) {
//         throw new RuntimeException("No questions found for the given exam.");
//     }

//     List<ExamSubtopicDistribution> distributions = distributionRepository.findByExam(exam);
//     if (distributions.isEmpty()) {
//         throw new RuntimeException("No distribution data found for the given exam.");
//     }

//     Map<Long, List<Question>> questionsBySubtopic = allQuestions.stream()
//             .collect(Collectors.groupingBy(q -> q.getSubtopic().getSubtopicId()));

//     List<Question> finalQuestions = new ArrayList<>();
//     int totalQuestionsRequired = 20; // Or you can make this configurable per exam

//     for (ExamSubtopicDistribution dist : distributions) {
//         Long subtopicId = dist.getSubtopic().getSubtopicId();
//         int subtopicQuestionCount =(int) Math.round((dist.getSubtopicPercentage() / 100f) * totalQuestionsRequired);

//         List<Question> subtopicQuestions = questionsBySubtopic.getOrDefault(subtopicId, new ArrayList<>());
//         if (subtopicQuestions.isEmpty()) continue;

//         Map<DifficultyLevel, List<Question>> questionsByDifficulty = subtopicQuestions.stream()
//                 .collect(Collectors.groupingBy(Question::getDifficultyLevel));

//         int basicCount = (int)Math.round((dist.getBasicPercentage() / 100f) * subtopicQuestionCount);
//         int intermediateCount = (int)Math.round((dist.getIntermediatePercentage() / 100f) * subtopicQuestionCount);
//         int advanceCount = subtopicQuestionCount - basicCount - intermediateCount;

//         finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.getOrDefault(DifficultyLevel.Basic, new ArrayList<>()), basicCount));
//         finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.getOrDefault(DifficultyLevel.Intermediate, new ArrayList<>()), intermediateCount));
//         finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.getOrDefault(DifficultyLevel.Advance, new ArrayList<>()), advanceCount));
//     }

//     // Fill any shortfall
//     if (finalQuestions.size() < totalQuestionsRequired) {
//         List<Question> remainingPool = allQuestions.stream()
//                 .filter(q -> !finalQuestions.contains(q))
//                 .collect(Collectors.toList());
//         Collections.shuffle(remainingPool);
//         finalQuestions.addAll(remainingPool.subList(0, Math.min(totalQuestionsRequired - finalQuestions.size(), remainingPool.size())));
//     }

//     Collections.shuffle(finalQuestions);
//     return finalQuestions;
// }

// //     public List<Question> generateCustomQuestions(GenerateQuestionsDTO request) {
// //     List<Question> allQuestions = questionRepository.findByExamExamId(request.getExamId());
// //     if (allQuestions.isEmpty()) {
// //         throw new RuntimeException("No questions found for the given exam.");
// //     }

// //     Map<Long, List<Question>> questionsBySubtopic = allQuestions.stream()
// //             .collect(Collectors.groupingBy(q -> q.getSubtopic().getSubtopicId()));

// //     List<Question> finalQuestions = new ArrayList<>();
// //     int totalQuestionsRequired = request.getTotalQuestions();

// //     for (SubtopicDifficultyRequest dist : request.getDistribution()) {
// //         int subtopicQuestionCount = Math.round((dist.getPercentage() / 100f) * totalQuestionsRequired);
// //         List<Question> questions = questionsBySubtopic.getOrDefault(dist.getSubtopicId(), new ArrayList<>());
// //         if (questions.isEmpty()) continue;

// //         Map<DifficultyLevel, List<Question>> groupedByDifficulty = questions.stream()
// //                 .collect(Collectors.groupingBy(Question::getDifficultyLevel));

// //         int basicCount = Math.round((dist.getDifficultyDistribution().getBasic() / 100f) * subtopicQuestionCount);
// //         int intermediateCount = Math.round((dist.getDifficultyDistribution().getIntermediate() / 100f) * subtopicQuestionCount);
// //         int advanceCount = subtopicQuestionCount - basicCount - intermediateCount;

// //         finalQuestions.addAll(pickRandomQuestions(groupedByDifficulty.get(DifficultyLevel.Basic), basicCount));
// //         finalQuestions.addAll(pickRandomQuestions(groupedByDifficulty.get(DifficultyLevel.Intermediate), intermediateCount));
// //         finalQuestions.addAll(pickRandomQuestions(groupedByDifficulty.get(DifficultyLevel.Advance), advanceCount));
// //     }

// //     if (finalQuestions.size() < totalQuestionsRequired) {
// //         List<Question> remainingPool = allQuestions.stream()
// //                 .filter(q -> !finalQuestions.contains(q))
// //                 .collect(Collectors.toList());
// //         Collections.shuffle(remainingPool);
// //         finalQuestions.addAll(remainingPool.subList(0, Math.min(totalQuestionsRequired - finalQuestions.size(), remainingPool.size())));
// //     }

// //     Collections.shuffle(finalQuestions);
// //     return finalQuestions;
// // }

// public List<Question> generateCustomQuestion(GenerateQuestionsDTO request) {
//     List<Question> allQuestions = questionRepository.findByExamExamId(request.getExamId());
//     if (allQuestions.isEmpty()) {
//         throw new RuntimeException("No questions found for the given exam.");
//     }

//     Exam exam = examRepository.findById(request.getExamId())
//             .orElseThrow(() -> new RuntimeException("Exam not found"));

//             // Validate total subtopic percentage does not exceed 100%
//     double totalSubtopicPercent = request.getDistribution().stream()
//         .mapToDouble(SubtopicDifficultyRequest::getPercentage)
//         .sum();
//     if (totalSubtopicPercent > 100) {
//         throw new RuntimeException("Total subtopic percentage exceeds 100%. Found: " + totalSubtopicPercent + "%");
//     }

//     for (SubtopicDifficultyRequest dist : request.getDistribution()) {
//         if (dist.getSubtopicId() == null) {
//             throw new RuntimeException("Subtopic ID in distribution cannot be null");
//         }

//         SubTopic subtopic = subtopicRepo.findById(dist.getSubtopicId().longValue())
//                 .orElseThrow(() -> new RuntimeException("Subtopic not found for ID: " + dist.getSubtopicId()));

//         ExamSubtopicDistribution examDistribution = new ExamSubtopicDistribution();
//         examDistribution.setExam(exam);
//         examDistribution.setSubtopic(subtopic);
//         examDistribution.setSubtopicPercentage(dist.getPercentage());

//         SubtopicDifficultyRequest.DifficultyPercentages dp = dist.getDifficultyDistribution();
//         examDistribution.setBasicPercentage(dp.getBasic());
//         examDistribution.setIntermediatePercentage(dp.getIntermediate());
//         examDistribution.setAdvancePercentage(dp.getAdvance());

//         distributionRepository.save(examDistribution);
//     }

//     Map<Long, List<Question>> questionsBySubtopic = allQuestions.stream()
//             .collect(Collectors.groupingBy(q -> q.getSubtopic().getSubtopicId()));

//     List<Question> finalQuestions = new ArrayList<>();
//     int totalQuestionsRequired = request.getTotalQuestions();

//     for (SubtopicDifficultyRequest dist : request.getDistribution()) {
//         if (dist.getSubtopicId() == null) {
//             continue; // skip null subtopicId entries here
//         }
        
//         int subtopicQuestionCount = (int) Math.round((dist.getPercentage() / 100f) * totalQuestionsRequired);
        
//         // Use longValue() safely
//         Long subtopicId = dist.getSubtopicId().longValue();

//         List<Question> subtopicQuestions = questionsBySubtopic.getOrDefault(subtopicId, new ArrayList<>());
//         if (subtopicQuestions.isEmpty()) continue;

//         Map<DifficultyLevel, List<Question>> questionsByDifficulty = subtopicQuestions.stream()
//                 .collect(Collectors.groupingBy(Question::getDifficultyLevel));

//         SubtopicDifficultyRequest.DifficultyPercentages dp = dist.getDifficultyDistribution();

//         int basicCount = (int) Math.round((dp.getBasic() / 100f) * subtopicQuestionCount);
//         int intermediateCount = (int) Math.round((dp.getIntermediate() / 100f) * subtopicQuestionCount);
//         int advanceCount = subtopicQuestionCount - basicCount - intermediateCount;

//         finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.getOrDefault(DifficultyLevel.Basic, new ArrayList<>()), basicCount));
//         finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.getOrDefault(DifficultyLevel.Intermediate, new ArrayList<>()), intermediateCount));
//         finalQuestions.addAll(pickRandomQuestions(questionsByDifficulty.getOrDefault(DifficultyLevel.Advance, new ArrayList<>()), advanceCount));
//     }

//     if (finalQuestions.size() < totalQuestionsRequired) {
//         List<Question> remainingPool = allQuestions.stream()
//                 .filter(q -> !finalQuestions.contains(q))
//                 .collect(Collectors.toList());
//         Collections.shuffle(remainingPool);
//         finalQuestions.addAll(remainingPool.subList(0, Math.min(totalQuestionsRequired - finalQuestions.size(), remainingPool.size())));
//     }

//     Collections.shuffle(finalQuestions);
//     return finalQuestions;
// }

// private List<Question> pickRandomQuestions(List<Question> pool, int count) {
//     if (pool == null || pool.isEmpty()) return new ArrayList<>();
//     Collections.shuffle(pool);
//     return pool.subList(0, Math.min(count, pool.size()));
// }

// public List<ExamSubtopicDistribution> getExamDistributions(int examId) {
//         Exam exam = examRepository.findById(examId)
//                 .orElseThrow(() -> new RuntimeException("Exam not found"));
//         List<ExamSubtopicDistribution> distributions = distributionRepository.findByExam(exam);
//         if (distributions.isEmpty()) {
//             throw new RuntimeException("No distribution data found for the given exam.");
//         }
//         return distributions;
//     }

// @Transactional
// public void updateSubtopicPercentages(GenerateQuestionsDTO request) {
//     Exam exam = examRepository.findById(request.getExamId())
//             .orElseThrow(() -> new RuntimeException("Exam not found"));

//     double totalSubtopicPercentage = request.getDistribution().stream()
//             .mapToDouble(SubtopicDifficultyRequest::getPercentage)
//             .sum();
//     if (totalSubtopicPercentage > 100) {
//         throw new RuntimeException("Total subtopic percentage exceeds 100%. Found: " + totalSubtopicPercentage + "%");
//     }

//     for (SubtopicDifficultyRequest dist : request.getDistribution()) {
//         Integer subtopicId = dist.getSubtopicId();
//         if (subtopicId == null) {
//             throw new RuntimeException("Subtopic ID in distribution cannot be null");
//         }

//         ExamSubtopicDistribution existingDistribution =
//                 distributionRepository.findByExamAndSubtopic_SubtopicId(exam, subtopicId)
//                         .orElseThrow(() -> new RuntimeException("Distribution not found for subtopic ID: " + subtopicId));

//         existingDistribution.setSubtopicPercentage(dist.getPercentage());

//         SubtopicDifficultyRequest.DifficultyPercentages dp = dist.getDifficultyDistribution();
//         existingDistribution.setBasicPercentage(dp.getBasic());
//         existingDistribution.setIntermediatePercentage(dp.getIntermediate());
//         existingDistribution.setAdvancePercentage(dp.getAdvance());

//         distributionRepository.save(existingDistribution);
//     }
// }

//     // public List<Question> getQuestionsByExam(int examId) {
//     // List<Question> allQuestions = questionRepository.findByExamExamId(examId);
//     //
//     // List<SubTopic> subtopics = allQuestions.stream()
//     // .map(Question::getSubtopic)
//     // .distinct()
//     // .sorted(Comparator.comparingLong(SubTopic::getSubtopicId))
//     // .collect(Collectors.toList());
//     //
//     // if (subtopics.size() < 2) {
//     // throw new RuntimeException("Insufficient number of subtopics for question
//     // distribution.");
//     // }
//     //
//     // List<Question> subtopic1Questions = new ArrayList<>();
//     // List<Question> subtopic2Questions = new ArrayList<>();
//     //
//     // for (Question question : allQuestions) {
//     // if (question.getSubtopic().getSubtopicId() ==
//     // subtopics.get(0).getSubtopicId()) {
//     // subtopic1Questions.add(question);
//     // } else if (question.getSubtopic().getSubtopicId() ==
//     // subtopics.get(1).getSubtopicId()) {
//     // subtopic2Questions.add(question);
//     // }
//     // }
//     //
//     // int subtopic1Count = (int) Math.ceil(subtopic1Questions.size() * 0.30);
//     // int subtopic2Count = (int) Math.ceil(subtopic2Questions.size() * 0.70);
//     //
//     // Collections.shuffle(allQuestions);
//     //
//     // return allQuestions;
//     // }

//     public void uploadSubTopicsWithQuestions(ExamUploadDTO dto) {
//         try (XSSFWorkbook workbook = new XSSFWorkbook(dto.getFile().getInputStream())) {

//             Subject subject = subjectRepository.findById(dto.getSubjectId())
//                     .orElseThrow(() -> new RuntimeException("Subject not found"));
//             Sheet sheet = workbook.getSheetAt(0);

//             Exam exam = new Exam();
//             exam.setTitle(dto.getTitle());
//             exam.setDescription(dto.getDescription());
//             exam.setStartTime(dto.getStartTime());
//             exam.setEndTime(dto.getEndTime());
//             exam.setDuration(dto.getDuration());
//             exam.setSubject(subject);
//             System.out.println("DTO startTime: " + dto.getStartTime());
//             System.out.println("DTO endTime: " + dto.getEndTime());

//             Exam savedExam = examRepository.save(exam);

//             for (Row row : sheet) {
//                 if (row.getRowNum() == 0)
//                     continue;

//                 Cell subtopicCell = row.getCell(0);
//                 if (subtopicCell == null || subtopicCell.getCellType() == CellType.BLANK) {
//                     System.out.println("Subtopic ID is missing at row " + row.getRowNum());
//                     continue;
//                 }
//                 long subtopicId = (long) subtopicCell.getNumericCellValue();

//                 if (subtopicId <= 0) {
//                     System.out.println("Invalid Subtopic ID: " + subtopicId);
//                     continue; // Skip this row if invalid ID
//                 }

//                 SubTopic topic = subtopicRepo.findById(subtopicId)
//                         .orElseThrow(() -> new RuntimeException("Invalid Subtopic id" + subtopicId));

//                 boolean exists = subtopicRepo.existsByNameAndSubject(topic.getName(), subject);
//                 if (!exists) {
//                     throw new RuntimeException("SubTopic not found");
//                 }

//                 String questionText = row.getCell(1).getStringCellValue();
//                 String qType = row.getCell(3).getStringCellValue();
//                 String difficultyLevel = row.getCell(4).getStringCellValue();
//                 int marks = (int) row.getCell(row.getLastCellNum() - 1).getNumericCellValue();

//                 String correctOptionLetter = row.getCell(row.getLastCellNum() - 2).getStringCellValue();
//                 List<String> correctOptions = parseCorrectOptions(correctOptionLetter);
//                 System.out.println("Correct Options : " + correctOptions);

//                 List<String> options = new ArrayList<>();
//                 for (int i = 5; i < row.getLastCellNum() - 2; i++) {
//                     Cell cell = row.getCell(i);
//                     if (cell == null || cell.getCellType() == CellType.BLANK)
//                         break;
//                     String value = "";
//                     switch (cell.getCellType()) {
//                         case STRING:
//                             value = cell.getStringCellValue().trim();
//                             break;
//                         case NUMERIC:
//                             value = String.valueOf(cell.getNumericCellValue()).trim();
//                             break;
//                         case BOOLEAN:
//                             value = String.valueOf(cell.getBooleanCellValue()).trim();
//                             break;
//                         default:
//                             value = "";
//                     }

//                     options.add(value);
//                 }

//                 System.out.println("Options : " + options);

//                 Set<Integer> correctIndexes = Arrays.stream(correctOptionLetter.split(","))
//                         .map(String::trim)
//                         .map(letter -> letter.charAt(0) - 'A')
//                         .filter(index -> index >= 0 && index < options.size())
//                         .collect(Collectors.toSet());

//                 if (correctIndexes.isEmpty()) {
//                     throw new RuntimeException("No valid correct options provided for: " + questionText);
//                 }

//                 Question question = new Question();
//                 question.setQuestionText(questionText);
//                 question.setQuestionType(QuestionType.valueOf(qType));
//                 question.setDifficultyLevel(DifficultyLevel.valueOf(difficultyLevel));
//                 question.setMarks(marks);
//                 question.setSubtopic(topic);
//                 question.setExam(exam);

//                 System.out.println("Options Size : " + options.size());
//                 for (int i = 0; i < options.size(); i++) {
//                     QuestionOption option = new QuestionOption();
//                     option.setOptionText(options.get(i));
//                     option.setIsCorrect(correctIndexes.contains(i));
//                     question.addQuestionOption(option);
//                 }

//                 savedExam.addQuestion(question);
//             }

//             examRepository.save(savedExam);

//         } catch (IOException e) {
//             throw new RuntimeException("Error processing file " + e.getMessage());
//         } catch (IllegalStateException ie) {
//             throw new RuntimeException("Please upload xl by checking the data format!");
//         }
//     }

//     public static List<String> parseCorrectOptions(String correctOptionInput) {
//         correctOptionInput = correctOptionInput.trim();

//         if (!isValidInput(correctOptionInput)) {
//             throw new IllegalArgumentException("Invalid characters in input! Only letters A-E and commas are allowed.");
//         }

//         StringTokenizer tokenizer = new StringTokenizer(correctOptionInput, ",");
//         List<String> options = new ArrayList<>();

//         while (tokenizer.hasMoreTokens()) {
//             String token = tokenizer.nextToken().trim().toUpperCase();
//             if (!isValidOption(token)) {
//                 throw new IllegalArgumentException(
//                         "Invalid option detected: " + token + ". Only options A-E are allowed.");
//             }
//             options.add(token);
//         }

//         return options;
//     }

//     private static boolean isValidInput(String input) {
//         String regex = "^[A-Ea-e, ]+$";
//         return Pattern.matches(regex, input);
//     }

//     private static boolean isValidOption(String option) {
//         return option.matches("[A-E]");
//     }

//     public List<Question> getAllQuestionsByExam(int examId){
//         Exam exam = examRepository.findById(examId)
//                                             .orElseThrow(()-> new RuntimeException("Questions not found"));
//         return questionRepository.findByExam(exam);
//     }
// }

