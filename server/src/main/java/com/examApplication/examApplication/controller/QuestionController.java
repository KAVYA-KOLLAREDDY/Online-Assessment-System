package com.examApplication.examApplication.controller;

import com.examApplication.examApplication.dto.ExamQuestionsResponseDTO;
import com.examApplication.examApplication.dto.ExamUploadDTO;
import com.examApplication.examApplication.dto.GenerateQuestionsDTO;
import com.examApplication.examApplication.dto.SubtopicDistributionDTO;
import com.examApplication.examApplication.entity.ExamSubtopicDistribution;
import com.examApplication.examApplication.entity.Question;
import com.examApplication.examApplication.entity.SubTopic;
import com.examApplication.examApplication.service.QuestionService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;
    
    @GetMapping("/exam/{examId}")
    public ResponseEntity<ExamQuestionsResponseDTO> getQuestionsByExam(@PathVariable int examId) {
        return ResponseEntity.ok(questionService.getQuestionsByExam(examId));
    }

    // @GetMapping("/exam/{examId}")
    // public ResponseEntity<List<Question>> getQuestionsByExam(@PathVariable int examId) {
    //     return ResponseEntity.ok(questionService.getQuestionsByExam(examId));
    // }

    @GetMapping("/exams/{examId}")
    public ResponseEntity<List<Question>> getQuestionByExam(@PathVariable int examId) {
        return ResponseEntity.ok(questionService.getAllQuestionsByExam(examId));
    }

    @PostMapping("/upload-latest")
    public ResponseEntity<Void> uploadLatestQuestions(@ModelAttribute ExamUploadDTO dto) {
        questionService.uploadSubTopicsWithQuestions(dto);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/generate-questions")
    public ResponseEntity<List<Question>> generateCustomQuestions(@RequestBody GenerateQuestionsDTO request) {
        List<Question> result = questionService.generateCustomQuestion(request);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/update-distribution")
    public ResponseEntity<List<ExamSubtopicDistribution>> updateSubtopicDistribution(@RequestBody GenerateQuestionsDTO request) {
        try {
            List<ExamSubtopicDistribution> updated = questionService.updateSubtopicPercentages(request);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
    }

    // @GetMapping("/exam/{examId}/distributions")
    // public ResponseEntity<List<ExamSubtopicDistribution>> getExamDistributions(@PathVariable int examId) {
    //     try {
    //         List<ExamSubtopicDistribution> distributions = questionService.getExamDistributions(examId);
    //         return ResponseEntity.ok(distributions);
    //     } catch (RuntimeException e) {
    //         return ResponseEntity.badRequest().body(null);
    //     }
    // }

    @GetMapping("/exam/{examId}/distributions")
    public ResponseEntity<List<SubtopicDistributionDTO>> getExamDistributions(@PathVariable int examId) {
        try {
            List<SubtopicDistributionDTO> dtoList = questionService.getExamDistributions(examId);
            return ResponseEntity.ok(dtoList);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // New endpoint to check if an exam has existing distributions
    @GetMapping("/exam/{examId}/has-distributions")
    public ResponseEntity<Boolean> hasExamDistributions(@PathVariable int examId) {
        try {
            boolean hasDistributions = questionService.hasExamDistributions(examId);
            return ResponseEntity.ok(hasDistributions);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(false);
        }
    }
}
// @RestController
// @RequestMapping("/questions")
// @RequiredArgsConstructor
// public class QuestionController {

//     private final QuestionService questionService;

//     @GetMapping("/exam/{examId}")
//     public ResponseEntity<List<Question>> getQuestionsByExam(@PathVariable int examId) {
//         return ResponseEntity.ok(questionService.getQuestionsByExam(examId));
//     }
//     @GetMapping("/exams/{examId}")
//     public ResponseEntity<List<Question>> getQuestionByExam(@PathVariable int examId) {
//         return ResponseEntity.ok(questionService.getAllQuestionsByExam(examId));
//     }
   
//     @PostMapping("/upload-latest")
//     public ResponseEntity<Void> uploadLatestQuestions(@ModelAttribute ExamUploadDTO dto) {
// //        questionService.uploadQuestionsAndOptions(dto);
//     	questionService.uploadSubTopicsWithQuestions(dto);
//         return ResponseEntity.status(HttpStatus.ACCEPTED).build();
//     }

//     @PostMapping("/generate-questions")
//     public ResponseEntity<List<Question>> generateCustomQuestions(@RequestBody GenerateQuestionsDTO request) {
//         List<Question> result = questionService.generateCustomQuestion(request);
//         return ResponseEntity.ok(result);
//     }

//     @PutMapping("/update-distribution")
//     public ResponseEntity<String> updateSubtopicDistribution(@RequestBody GenerateQuestionsDTO request) {
//         try {
//             questionService.updateSubtopicPercentages(request);
//             return ResponseEntity.ok("Subtopic distribution updated successfully.");
//         } catch (RuntimeException e) {
//             return ResponseEntity.badRequest().body("Error: " + e.getMessage());
//         }
//     }

//     @GetMapping("/exam/{examId}/distributions")
//     public ResponseEntity<List<ExamSubtopicDistribution>> getExamDistributions(@PathVariable int examId) {
//         try {
//             List<ExamSubtopicDistribution> distributions = questionService.getExamDistributions(examId);
//             return ResponseEntity.ok(distributions);
//         } catch (RuntimeException e) {
//             return ResponseEntity.badRequest().body(null);
//         }
//     }
    
// }
