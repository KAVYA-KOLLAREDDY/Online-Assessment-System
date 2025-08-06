package com.examApplication.examApplication.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.examApplication.examApplication.dto.ExamResultResponseDTO;
import com.examApplication.examApplication.repository.ExamRepository;
import com.examApplication.examApplication.repository.ExamResultRepository;
import com.examApplication.examApplication.repository.QuestionOptionRepository;
import com.examApplication.examApplication.repository.QuestionRepository;
import com.examApplication.examApplication.repository.StudentResponseRepository;
import com.examApplication.examApplication.repository.SubTopicRepository;
import com.examApplication.examApplication.repository.SubjectRepository;
import com.examApplication.examApplication.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tests")
@RequiredArgsConstructor
public class TestController {
	private final ExamRepository examRepo;
	private final ExamResultRepository examResultRepo;
	private final QuestionOptionRepository questOptRepo;
	private final QuestionRepository questRepo;
	private final UserRepository userRepo;
	private final StudentResponseRepository studResponseRepository;
	private final SubjectRepository subRepo;
	private final SubTopicRepository subTopicRepo;

//	@GetMapping
//	public ResponseEntity<List<ExamResultResponseDTO>> getRepo() {
//		System.out.println(examResultRepo.findAll().size());
//		var res = examResultRepo.findAll()
//				.stream()
//				.map(examRes -> {
//					System.out.println(examRes.getExam());
//					return ExamResultResponseDTO.builder()
//							.examName(examRes.getExam() == null ? null : examRes.getExam().getTitle())
//							.studentName(examRes.getStudent().getName())
//							.obtainedMarks(examRes.getObtainedMarks())
//							.totalMarks(examRes.getTotalMarks())
//							.percentage(examRes.getPercentage())
//							.isQualified(examRes.getPassed())
//							.build();
//				})
//				.toList();
//		return ResponseEntity.ok(res);
//	}
}
