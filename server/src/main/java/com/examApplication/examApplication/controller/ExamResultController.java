package com.examApplication.examApplication.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.examApplication.examApplication.entity.ExamResult;
import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.helpers.UserUtils;
import com.examApplication.examApplication.repository.ExamResultRepository;
import com.examApplication.examApplication.repository.UserRepository;
import com.examApplication.examApplication.service.ExamResultService;

import lombok.RequiredArgsConstructor;

import com.examApplication.examApplication.dto.ExamResultRequest;
import com.examApplication.examApplication.dto.ExamResultResponseDTO;
import com.examApplication.examApplication.dto.ExamResultTopicDTO;
import com.examApplication.examApplication.dto.QuestionAnalysisDTO;
import com.examApplication.examApplication.dto.SubtopicDifficultyAnalysisDTO;

@RestController
@RequestMapping("/exam-results")
@RequiredArgsConstructor
public class ExamResultController {

    
    private final ExamResultService examResultService;
    private final UserRepository userRepository;
    private final ExamResultRepository examResultRepository;
    

    @PostMapping("/calculate/{studentId}/{examId}")
    public ResponseEntity<ExamResultTopicDTO> calculateExamResult(@PathVariable Integer studentId, @PathVariable Integer examId, @RequestBody ExamResultRequest examRequest) {
        ExamResultTopicDTO result = examResultService.calcExamRes(examRequest);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/exam/{examId}/student/{studentId}")
    public ResponseEntity<ExamResult> getExamResult(@PathVariable Integer examId, @PathVariable Integer studentId) {
        return ResponseEntity.ok(examResultService.getExamResult(examId, studentId));
    }

    @GetMapping("/all")
    public ResponseEntity<List<ExamResultResponseDTO>> getAllExamResults() {
        List<ExamResultResponseDTO> results = examResultService.getAllExamResults();
        return ResponseEntity.ok(results);
    }

    
    @GetMapping("/responses/latest/{examId}")
    public ResponseEntity<?> getLatestExamResult(@PathVariable Integer examId) {
        User user = UserUtils.getUser();

        List<ExamResult> results = examResultRepository
                .findByExam_ExamIdAndUser_UserIdOrderByCompletedAtDesc(examId, user.getUserId());

        if (results.isEmpty()) {
            return ResponseEntity.noContent().build(); // ✅ Don't throw — just return 204
        }

        ExamResult latestResult = results.get(0);
        return ResponseEntity.ok(examResultService.getExamResultById(latestResult.getResultId()));
    }


@GetMapping("/result/{id}/difficulty-analysis")
public ResponseEntity<List<SubtopicDifficultyAnalysisDTO>> getDifficultyStats(@PathVariable int id) {
    return ResponseEntity.ok(examResultService.getDifficultyLevelStatsByResultId(id));
}

@GetMapping("/result/{id}/question-analysis")
public ResponseEntity<List<QuestionAnalysisDTO>> getQuestionAnalysis(@PathVariable int id) {
    return ResponseEntity.ok(examResultService.getQuestionAnalysisByResultId(id));
}


}
