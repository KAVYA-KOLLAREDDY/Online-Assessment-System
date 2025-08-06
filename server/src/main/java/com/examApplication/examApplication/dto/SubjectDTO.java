package com.examApplication.examApplication.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class SubjectDTO {
	private Long subjectId;
	private String subjectName;
	private String description;
	private String status;
}
