package com.examApplication.examApplication.dto;

import java.util.List;
import java.util.Map;

import com.examApplication.examApplication.entity.Question;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class ExamQuestionsResponseDTO {
    private final List<Question> questions;
    private final Map<String, Integer> subtopicQuestionCounts;

}
