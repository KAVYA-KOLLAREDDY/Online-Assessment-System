package com.examApplication.examApplication.dto;

import java.time.LocalDate;

import com.examApplication.examApplication.model.ExamStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ExamDTO {
	private Integer examId;
	private String title;
	private String description;
	private LocalDate startTime;
    private LocalDate endTime;
    private ExamStatus examStatus;
	private Integer attempts;
	private boolean isPassed;
	private Integer duration;
	private Integer createdBy;
}
