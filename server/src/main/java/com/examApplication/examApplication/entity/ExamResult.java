package com.examApplication.examApplication.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.examApplication.examApplication.entity.auth.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exam_results")
@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class ExamResult {
    @Id
    @Column(name = "result_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resultId;

    @ManyToOne
    @JoinColumn(name = "exam_id")
    private Exam exam;

//    @ManyToOne
//    @JoinColumn(name = "subtopic_id")  
//    private SubTopic subtopic;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; 

    @Column(nullable = false)
    private int totalMarks;

    @Column(nullable = false)
    private double obtainedMarks;

    @Column(nullable = false)
    private double percentage;

    @Column(nullable = false)
    private Boolean passed;

    @CreationTimestamp
    private LocalDateTime completedAt;
}
