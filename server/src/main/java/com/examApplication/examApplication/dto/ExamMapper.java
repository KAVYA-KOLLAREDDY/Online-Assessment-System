package com.examApplication.examApplication.dto;

import java.util.List;

import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.ExamResult;
import com.examApplication.examApplication.entity.auth.User;

import lombok.Data;

public class ExamMapper {

	public static ExamDTO toDTO(Exam exam, User currentUser) {
	    List<ExamResult> userResults = exam.getResults().stream()
	        .filter(result -> result.getUser().getUserId() == currentUser.getUserId())
	        .toList();

	    boolean isPassed = userResults.stream().anyMatch(result -> result.getPassed());
	    int attempts = userResults.size();

	    return ExamDTO.builder()
	        .examId(exam.getExamId())
	        .title(exam.getTitle())
	        .description(exam.getDescription())
	        .startTime(exam.getStartTime())
	        .endTime(exam.getEndTime())
	        .examStatus(exam.getExamStatus())
	        .attempts(attempts)
	        .isPassed(isPassed)
	        .createdBy(exam.getCreatedBy() != null ? exam.getCreatedBy().getUserId() : null)
	        .duration(exam.getDuration())
	        .build();
	}

}
