package com.examApplication.examApplication.dto;

import java.util.List;

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
public class ExamResultTopicDTO {
	private Integer resultId;
	private List<TopicResultDTO> topics;
	private Integer totalMarks;
	private Double obtainedMarks;
	private Double percentage;
	private Boolean passed;
	private Integer remainingAttempts;
}
