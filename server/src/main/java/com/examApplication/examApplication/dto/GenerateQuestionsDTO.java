package com.examApplication.examApplication.dto;

import java.util.List;

import lombok.Data;

@Data
public class GenerateQuestionsDTO {
    private int examId;
    private int totalQuestions;
    private String examType;
    private List<SubtopicDifficultyRequest> distribution;
}
