package com.examApplication.examApplication.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.ExamSubtopicDistribution;
import com.examApplication.examApplication.entity.SubTopic;

public interface ExamSubtopicDistributionRepository extends JpaRepository<ExamSubtopicDistribution, Integer> {
        List<ExamSubtopicDistribution> findByExam(Exam exam);
        Optional<ExamSubtopicDistribution> findByExamAndSubtopic_SubtopicId(Exam exam, Integer subtopicId);
        Optional<Exam> findByExamAndSubtopic(Exam exam, SubTopic subtopic);

} 
