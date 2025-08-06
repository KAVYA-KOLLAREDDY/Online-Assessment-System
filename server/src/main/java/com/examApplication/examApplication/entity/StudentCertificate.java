package com.examApplication.examApplication.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.examApplication.examApplication.entity.auth.User;

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
@Table(name = "student_certificates")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentCertificate {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer certificateId;
	
	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
	@ManyToOne
	@JoinColumn(name = "exam_id")
	private Exam exam;

	private String certificatePath;
	
	@CreationTimestamp
	private LocalDateTime issuedAt;
}
