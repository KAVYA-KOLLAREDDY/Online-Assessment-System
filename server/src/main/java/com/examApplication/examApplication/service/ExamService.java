package com.examApplication.examApplication.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.examApplication.examApplication.dto.ExamDTO;
import com.examApplication.examApplication.dto.ExamMapper;
import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.ExamResult;
import com.examApplication.examApplication.entity.Subject;
import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.helpers.UserUtils;
import com.examApplication.examApplication.model.ExamStatus;
import com.examApplication.examApplication.repository.ExamRepository;
import com.examApplication.examApplication.repository.StudentAttemptRepository;
import com.examApplication.examApplication.repository.SubTopicRepository;
import com.examApplication.examApplication.repository.SubjectRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExamService {

    private final ExamRepository examRepository;
    private final SubTopicRepository subtopicRepository;
    private final SubjectRepository subjectRepository;
    private final AuthService authService;
    private final StudentAttemptRepository studentAttemptRepository;
    private final QuestionService questionService;

    public List<ExamDTO> getExamsCreatedByCurrentUser() {
        User currentUser = authService.getUser();
        return examRepository.findByCreatedBy(currentUser)
                             .stream()
                             .map(exam -> ExamMapper.toDTO(exam, currentUser))
                             .collect(Collectors.toList());
    }

    public List<ExamDTO> createExamWithSubject(Exam exam, Long subjectId) {
        Subject subject = subjectRepository.findById(subjectId)
            .orElseThrow(() -> new RuntimeException("Subject with ID " + subjectId + " not found"));

        User currentUser = authService.getUser();
        exam.setSubject(subject);
        exam.setCreatedBy(currentUser);

        examRepository.save(exam);

        return examRepository.findAll()
                             .stream()
                             .map(e -> ExamMapper.toDTO(e, currentUser))
                             .collect(Collectors.toList());
    }

    public List<ExamDTO> getAllExams() {
        User currentUser = UserUtils.getUser();

        return examRepository.findAll()
                .stream()
                .filter(exam -> questionService.hasExamDistributions(exam.getExamId())) //remove this line if we don't need any percentage distribution for exam
                .map(exam -> {
                    updateExamStatus(exam);
                    return ExamMapper.toDTO(exam, currentUser);
                })
                .collect(Collectors.toList());
    }

    public ExamDTO getExamById(int id) {
        Exam exam = examRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Exam with ID " + id + " not found"));

        User currentUser = UserUtils.getUser();
        return ExamMapper.toDTO(exam, currentUser);
    }

    public ExamDTO updateExam(Integer examId, Exam updatedExam) {
        User currentUser = UserUtils.getUser();

        return examRepository.findById(examId).map(existingExam -> {
            existingExam.setTitle(updatedExam.getTitle());
            existingExam.setDescription(updatedExam.getDescription());
            existingExam.setStartTime(updatedExam.getStartTime());
            existingExam.setEndTime(updatedExam.getEndTime());
            return ExamMapper.toDTO(examRepository.save(existingExam), currentUser);
        }).orElseThrow(() -> new RuntimeException("Exam with ID " + examId + " not found"));
    }

    private void updateExamStatus(Exam exam) {
        LocalDate now = LocalDate.now();

        if (now.isBefore(exam.getStartTime())) {
            exam.setExamStatus(ExamStatus.SCHEDULED);
        } else if (!now.isBefore(exam.getStartTime()) && !now.isAfter(exam.getEndTime())) {
            exam.setExamStatus(ExamStatus.ONGOING);
        } else if (now.isAfter(exam.getEndTime())) {
            exam.setExamStatus(ExamStatus.COMPLETED);
        }

        examRepository.save(exam); // persist status change
    }

    public List<ExamDTO> deleteExam(int id) {
        if (!examRepository.existsById(id)) {
            throw new RuntimeException("Exam with ID " + id + " not found");
        }

        examRepository.deleteById(id);
        User currentUser = UserUtils.getUser();

        return examRepository.findAll()
                             .stream()
                             .map(exam -> ExamMapper.toDTO(exam, currentUser))
                             .collect(Collectors.toList());
    }
}
