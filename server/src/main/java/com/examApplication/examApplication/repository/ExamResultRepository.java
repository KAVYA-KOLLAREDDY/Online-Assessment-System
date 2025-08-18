package com.examApplication.examApplication.repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.ExamResult;
import com.examApplication.examApplication.entity.auth.User;

public interface ExamResultRepository extends JpaRepository<ExamResult, Integer> {

    ExamResult findByExam_ExamIdAndUser_UserId(Integer examId, Integer userId);
    
    Optional<ExamResult> findTopByExam_ExamIdAndUser_UserIdAndPassedIsTrueOrderByCompletedAtDesc(
    Integer examId, Integer userId);

    Optional<ExamResult> findByResultId(Integer id);
    
    int countByExam_ExamIdAndUser_UserId(Integer examId, Integer userId);
    
    int countByExamAndUser(Exam exam, User user);

    int countByUserAndExam(User user, Exam exam);

    List<ExamResult> findByExam_ExamIdAndUser_UserIdOrderByCompletedAtDesc(Integer examId, Integer userId);

	Optional<ExamResult> findByUserAndExam(User user, Exam exam);


}
