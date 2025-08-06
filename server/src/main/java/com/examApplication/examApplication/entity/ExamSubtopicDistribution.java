package com.examApplication.examApplication.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam_subtopic_distribution")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExamSubtopicDistribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "exam_id", nullable = false)
    @JsonBackReference 
    private Exam exam;

    @ManyToOne
    @JoinColumn(name = "subtopic_id", nullable = false)
    @JsonBackReference
    private SubTopic subtopic;

    @Column(name = "subtopic_percentage", nullable = false)
    private Double subtopicPercentage;

    @Column(name = "basic_percentage", nullable = false)
    private Double basicPercentage;

    @Column(name = "intermediate_percentage", nullable = false)
    private Double intermediatePercentage;

    @Column(name = "advance_percentage", nullable = false)
    private Double advancePercentage;

}
