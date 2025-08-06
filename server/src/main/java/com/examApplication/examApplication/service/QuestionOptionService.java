package com.examApplication.examApplication.service;

import com.examApplication.examApplication.entity.QuestionOption;
import com.examApplication.examApplication.repository.QuestionOptionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionOptionService {

    private final QuestionOptionRepository questionOptionRepository;
    
    public Integer getAllQuestionOptions() {
    	return questionOptionRepository.findAll().size();
    }

    public List<QuestionOption> getOptionsByQuestionId(int questionId) {
        return questionOptionRepository.findByQuestionQuestionId(questionId);
    }

    public QuestionOption saveQuestionOption(QuestionOption questionOption) {
        return questionOptionRepository.save(questionOption);
    }

    public void addOptions(List<QuestionOption> options) {
        questionOptionRepository.saveAll(options);
    }
}
