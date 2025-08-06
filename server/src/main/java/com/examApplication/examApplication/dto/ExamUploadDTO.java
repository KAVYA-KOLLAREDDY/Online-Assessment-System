package com.examApplication.examApplication.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExamUploadDTO {
	private String title;
	private String description;
	public LocalDate startTime;
	public LocalDate endTime;
	public Integer duration;
	private MultipartFile file;
	private Long subjectId;
}
