package com.examApplication.examApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubTopicResultsDTO {
	private String subtopicName;
    private double percentage;
}
