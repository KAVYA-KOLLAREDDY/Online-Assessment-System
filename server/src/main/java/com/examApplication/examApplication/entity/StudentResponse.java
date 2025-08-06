package com.examApplication.examApplication.entity;

import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

import com.examApplication.examApplication.entity.auth.User;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "student_responses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer responseId;

    // @ManyToOne
    // @JoinColumn(name = "user_id", nullable = false)
    // private User user;

    @ManyToOne
    @JoinColumn(name = "attempt_id")
    private StudentAttempt attempt;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne
    @JoinColumn(name = "selected_option_id")
    private QuestionOption selectedOption;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime submittedAt;
}
