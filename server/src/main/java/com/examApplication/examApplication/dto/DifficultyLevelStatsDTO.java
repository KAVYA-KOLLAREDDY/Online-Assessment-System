package com.examApplication.examApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DifficultyLevelStatsDTO {
    private String level;
    private int totalQuestions;
    private int totalMarks;
    private int correct;
    private int incorrect;
    private int marksObtained;
}

