package com.examApplication.examApplication.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "subtopic_results")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SubtopicResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer subtopicResultId;

    @ManyToOne
    @JoinColumn(name = "exam_result_id")
    private ExamResult examResult;

    @ManyToOne
    @JoinColumn(name = "subtopic_id")
    private SubTopic subtopic;

    @Column(name = "total_marks", nullable = false)
    private Integer totalMarks;

    @Column(nullable = false)
    private double percentage;

    @Column(name = "passed", nullable = false)
    private Boolean passed;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}