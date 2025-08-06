package com.examApplication.examApplication.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class ExamResultRequest {
	private Integer examId;
	private String attemptStatus;
	private List<StudentResponseRequest> responses;
}
