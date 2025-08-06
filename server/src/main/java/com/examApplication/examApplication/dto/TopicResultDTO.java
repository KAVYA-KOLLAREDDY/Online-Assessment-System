package com.examApplication.examApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TopicResultDTO {
	private String topic;
	private Double percentage;
	private Boolean passed;
	private int totalQuestions; // New field
    private int correctQuestions;
}
