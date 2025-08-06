package com.examApplication.examApplication.dto;

import lombok.Data;

@Data
public class SubtopicResultDTO {
    private Integer examResultId;
    private Integer subtopicId;
    private Integer totalMarks;
    private Double percentage;
    private Boolean passed;
}