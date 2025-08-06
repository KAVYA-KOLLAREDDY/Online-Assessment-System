package com.examApplication.examApplication.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.examApplication.examApplication.dto.ExamResultRequest;
import com.examApplication.examApplication.dto.ExamResultTopicDTO;
import com.examApplication.examApplication.entity.ExamResult;
import com.examApplication.examApplication.service.ExamResultService;

@RestController
@RequestMapping("/responses")
@CrossOrigin(origins = "http://localhost:4200")
public class StudentResponseController {

    private final ExamResultService examResultService;

    @Autowired
    public StudentResponseController(ExamResultService resultService) {
        this.examResultService = resultService;
    }

    @PostMapping
    public ResponseEntity<ExamResultTopicDTO> saveResponse(@RequestBody ExamResultRequest examRequest) {
        return ResponseEntity.ok(examResultService.calcExamRes(examRequest));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamResultTopicDTO> getResponseById(@PathVariable Integer id) {
    	ExamResultTopicDTO examResult = examResultService.getExamResultById(id);
        return ResponseEntity.ok(examResult);
    }
}
