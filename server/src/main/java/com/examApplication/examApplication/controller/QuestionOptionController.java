package com.examApplication.examApplication.controller;

import com.examApplication.examApplication.entity.QuestionOption;
import com.examApplication.examApplication.service.QuestionOptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question-options")
@RequiredArgsConstructor
public class QuestionOptionController {

    private final QuestionOptionService questionOptionService;
    
    @GetMapping
    public Integer getAllQuestionOptions() {
    	return questionOptionService.getAllQuestionOptions();
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<QuestionOption>> getOptionsByQuestion(@PathVariable int questionId) {
        return ResponseEntity.ok(questionOptionService.getOptionsByQuestionId(questionId));
    }

    @PostMapping("/bulk")
    public ResponseEntity<String> addOptions(@RequestBody List<QuestionOption> options) {
        questionOptionService.addOptions(options);
        return ResponseEntity.ok("Options added successfully");
    }
}
