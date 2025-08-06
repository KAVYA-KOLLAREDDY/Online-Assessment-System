package com.examApplication.examApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExamResultResponseDTO {
    private String studentName;
    private String examName;
    private Double obtainedMarks;
    private Integer totalMarks;
    private Double percentage;
    private Boolean isQualified;
}
