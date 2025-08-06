package com.examApplication.examApplication.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.examApplication.examApplication.dto.SubtopicResultDTO;
import com.examApplication.examApplication.entity.SubtopicResult;
import com.examApplication.examApplication.repository.SubtopicResultRepository;

@Service
public class SubtopicResultService {

    @Autowired
    private SubtopicResultRepository repository;


    public List<SubtopicResult> getAllSubtopicResults() {
        return repository.findAll();
    }
}
