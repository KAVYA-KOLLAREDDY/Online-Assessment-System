package com.examApplication.examApplication.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.StudentAttempt;
import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.model.AttemptStatus;
import com.examApplication.examApplication.repository.StudentAttemptRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentAttemptService {

    private final StudentAttemptRepository studentAttemptRepository;

    public StudentAttempt updateAttemptStatus(User user, Exam exam, String attemptStatus) {
        try {
            List<StudentAttempt> attempts = studentAttemptRepository.findByUserAndExamOrderByAttemptCountDesc(user, exam);
            if (attempts.isEmpty()) {
                throw new RuntimeException("Attempts not found");
            }
            StudentAttempt firstAttempt = attempts.get(0);
            firstAttempt.setStatus(AttemptStatus.valueOf(attemptStatus));
            return studentAttemptRepository.save(firstAttempt);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid attempt status: " + attemptStatus, e);
        }
    }
}
