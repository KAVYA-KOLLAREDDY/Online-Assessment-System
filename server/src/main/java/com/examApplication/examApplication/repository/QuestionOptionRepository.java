package com.examApplication.examApplication.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examApplication.examApplication.entity.Question;
import com.examApplication.examApplication.entity.QuestionOption;

public interface QuestionOptionRepository extends JpaRepository<QuestionOption,Integer>{

	List<QuestionOption> findByQuestionQuestionId(int questionId);
	List<QuestionOption> findByQuestionAndIsCorrect(Question question, Boolean isCorrect);
}
