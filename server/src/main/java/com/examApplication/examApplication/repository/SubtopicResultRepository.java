package com.examApplication.examApplication.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examApplication.examApplication.entity.ExamResult;
import com.examApplication.examApplication.entity.SubtopicResult;

public interface SubtopicResultRepository extends JpaRepository<SubtopicResult, Integer> {

	List<SubtopicResult> findByExamResult(ExamResult examResult);
	
}