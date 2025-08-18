package com.examApplication.examApplication.controller;

import com.examApplication.examApplication.dto.ExamDTO;
import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.service.ExamService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @GetMapping("/all")
    public ResponseEntity<List<ExamDTO>> getAllExams() {
        return ResponseEntity.ok(examService.getAllExams());
    }

    @GetMapping("/my")
    public ResponseEntity<List<ExamDTO>> getMyExams() {
        return ResponseEntity.ok(examService.getExamsCreatedByCurrentUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamDTO> getExamById(@PathVariable int id) {
        return ResponseEntity.ok(examService.getExamById(id));
    }

    @PostMapping("/subject/{subjectId}")
    public ResponseEntity<List<ExamDTO>> createExamWithSubjects(
            @RequestBody Exam exam,
            @PathVariable Long subjectId
    ) {
        return ResponseEntity.ok(examService.createExamWithSubject(exam, subjectId));
    }

    @PutMapping("/{examId}")
    public ResponseEntity<ExamDTO> updateExam(
            @PathVariable Integer examId,
            @RequestBody Exam updatedExam
    ) {
        return ResponseEntity.ok(examService.updateExam(examId, updatedExam));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<List<ExamDTO>> deleteExam(@PathVariable int id) {
        return ResponseEntity.ok(examService.deleteExam(id));
    }
}
