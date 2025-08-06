package com.examApplication.examApplication.service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.examApplication.examApplication.entity.StudentResponse;
import com.examApplication.examApplication.repository.QuestionOptionRepository;
import com.examApplication.examApplication.repository.QuestionRepository;
import com.examApplication.examApplication.repository.StudentResponseRepository;

@Service
public class StudentResponseService {

    @Autowired
    private StudentResponseRepository studentResponseRepository;

    public List<StudentResponse> saveResponse(List<StudentResponse> responses) {
        List<StudentResponse> savedResponses = studentResponseRepository.saveAll(responses);
        
        savedResponses.forEach(response -> {
            System.out.println("Saved Response - Question ID: " + response.getQuestion().getQuestionId() + 
                               ", Selected Option ID: " + response.getSelectedOption().getOptionId());
        });
        
        return savedResponses;
    }
}
