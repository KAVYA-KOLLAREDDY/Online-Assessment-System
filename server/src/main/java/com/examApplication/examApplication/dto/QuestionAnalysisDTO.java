package com.examApplication.examApplication.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class QuestionAnalysisDTO {
    private Integer questionId;
    private String questionText;
    private String difficultyLevel;
    private List<String> correctAnswers;
    private List<String> selectedAnswers;
}

