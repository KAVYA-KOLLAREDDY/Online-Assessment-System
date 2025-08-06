package com.examApplication.examApplication.controller;

import java.util.List;
import java.util.Map;

import javax.management.RuntimeErrorException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.examApplication.examApplication.dto.StudentAttemptDTO;
import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.StudentAttempt;
import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.helpers.UserUtils;
import com.examApplication.examApplication.model.AttemptStatus;
import com.examApplication.examApplication.repository.ExamRepository;
import com.examApplication.examApplication.repository.StudentAttemptRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/attempts")
@RequiredArgsConstructor
public class StudentAttemptController {

    private final StudentAttemptRepository studentAttemptRepository;
    private final ExamRepository examRepository;
    private static final int MAX_ATTEMPTS = 3;

    @PostMapping
    public ResponseEntity<Void> createAttempt(@RequestBody Map<String, Object> attemptStatus) {
    Integer examId = (int) attemptStatus.get("examId");
    User user = UserUtils.getUser();
    Exam exam = examRepository.findById(examId).orElseThrow(() -> new RuntimeException("Exam not found"));

    
    // 🔴 Count previous attempts
    int totalAttempts = studentAttemptRepository.countByUserAndExam(user, exam);

    if (totalAttempts >= MAX_ATTEMPTS) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(null); // or throw new RuntimeException("Maximum attempts reached.");
    }

    boolean hasOngoing = studentAttemptRepository.existsByUserAndExamAndStatus(user, exam, AttemptStatus.ON_GOING);
    if (hasOngoing) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Already exists
    }
    // 🟢 Allow new attempt
    StudentAttempt studentAttempt = new StudentAttempt();
    studentAttempt.setExam(exam);
    studentAttempt.setUser(user);
    studentAttempt.setAttemptCount(totalAttempts + 1);
    studentAttempt.setStatus(AttemptStatus.ON_GOING);

    studentAttemptRepository.save(studentAttempt);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
}

    @PutMapping
    public ResponseEntity<Void> updateAttemptStatus(@RequestBody StudentAttemptDTO attemptDTO ){
        User user = UserUtils.getUser();
        Exam exam = examRepository.findById(attemptDTO.getExamId()).orElseThrow(()-> new RuntimeException("Exam not found"));
        
        List<StudentAttempt> attempts=studentAttemptRepository.findByUserAndExamOrderByAttemptCountDesc(user, exam);

        if(attempts.size()==0){
            throw new RuntimeException("Attempts not found");
        }
        StudentAttempt firstAttempt =attempts.get(0);
        firstAttempt.setStatus(AttemptStatus.valueOf(attemptDTO.getAttemptStatus()));
        studentAttemptRepository.save(firstAttempt);
        return ResponseEntity.ok().build();
    }

}
