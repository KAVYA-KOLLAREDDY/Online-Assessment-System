package com.examApplication.examApplication.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.examApplication.examApplication.dto.SubtopicResultDTO;
import com.examApplication.examApplication.entity.SubtopicResult;
import com.examApplication.examApplication.service.SubtopicResultService;

@RestController
@RequestMapping("/api/subtopic-results")
public class SubtopicResultController {

    @Autowired
    private SubtopicResultService service;

    @GetMapping
    public List<SubtopicResult> getAllSubtopicResults() {
        return service.getAllSubtopicResults();
    }
}
