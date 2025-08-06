package com.examApplication.examApplication.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.model.ExamStatus;
import com.examApplication.examApplication.model.ExamType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "exams")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Exam {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer examId;
    
    @Column(nullable = false, length = 255)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private LocalDate startTime;
    
    private LocalDate endTime;
    
    private Integer duration;

    private Integer totalQuestions;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="created_by")
    @JsonBackReference
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    @JsonBackReference
    private Subject subject;

    @OneToMany(mappedBy = "exam")
    @JsonManagedReference
    private List<StudentAttempt> studentAttempts;

    @Enumerated(EnumType.STRING)
    private ExamStatus examStatus = ExamStatus.SCHEDULED;
    
    @Enumerated(EnumType.STRING)
    private ExamType examType = ExamType.Programming; 
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonManagedReference
    private List<Question> questions = new ArrayList<>();
    
    @OneToMany(mappedBy = "exam")
    @JsonManagedReference
    private List<ExamResult> results = new ArrayList<>(); 
    
    public void addQuestion(Question question) {
    	if(questions == null) {
    		questions = new ArrayList<>();
    	}
    	questions.add(question);
    	question.setExam(this);
    }
    
}
