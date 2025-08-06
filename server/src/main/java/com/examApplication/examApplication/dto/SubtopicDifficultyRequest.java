package com.examApplication.examApplication.dto;

import lombok.Data;

@Data
public class SubtopicDifficultyRequest {
    private Integer subtopicId;
    private Double percentage;

    private DifficultyPercentages difficultyDistribution;

    @Data
    public static class DifficultyPercentages {
        private Double basic;
        private Double intermediate;
        private Double advance;
    }
}
