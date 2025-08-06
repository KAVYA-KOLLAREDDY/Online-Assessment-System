package com.examApplication.examApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubtopicDistributionDTO {
    private Integer id;
    private Long subtopicId;
    private String subtopicName;
    private Double subtopicPercentage;
    private Double basicPercentage;
    private Double intermediatePercentage;
    private Double advancePercentage;
}
