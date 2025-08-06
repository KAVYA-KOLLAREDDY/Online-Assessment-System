package com.examApplication.examApplication.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.Question;
import com.examApplication.examApplication.entity.StudentAttempt;
import com.examApplication.examApplication.entity.StudentResponse;
import com.examApplication.examApplication.entity.SubTopic;
import com.examApplication.examApplication.entity.auth.User;

public interface StudentResponseRepository extends JpaRepository<StudentResponse, Integer> {

    List<StudentResponse> findByAttemptUserUserIdAndAttemptExamExamId(Integer userId, Integer examId);

    List<StudentResponse> findByQuestionSubtopicSubtopicIdAndAttemptUserUserId(Long subtopicId, Integer userId);

    List<StudentResponse> findByQuestionSubtopicAndAttemptUser(SubTopic subtopic, User user);

    List<StudentResponse> findByAttemptExamAndQuestionSubtopicAndAttemptUser(Exam exam, SubTopic subtopic, User user);

    List<StudentResponse> findByAttemptAndQuestionSubtopic(StudentAttempt attempt, SubTopic subtopic);

    List<StudentResponse> findByAttempt(StudentAttempt attempt);

    List<StudentResponse> findByAttemptAndQuestion(StudentAttempt attempt, Question question);
}
