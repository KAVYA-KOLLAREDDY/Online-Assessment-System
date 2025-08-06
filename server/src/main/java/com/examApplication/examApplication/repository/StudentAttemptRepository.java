package com.examApplication.examApplication.repository;

import java.util.List;
import java.util.Optional;

import org.apache.xmlbeans.impl.xb.xsdschema.Attribute.Use;
import org.springframework.data.jpa.repository.JpaRepository;

import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.StudentAttempt;
import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.model.AttemptStatus;

public interface StudentAttemptRepository extends JpaRepository<StudentAttempt,Integer>{

    Integer countByUserAndExam(User user, Exam exam);

    List<StudentAttempt> findByUserAndExamOrderByAttemptCountDesc(User user, Exam exam);

    Optional<StudentAttempt> findByExamAndUserAndAttemptCount(Exam exam, User user, int attemptCount);

    Optional<StudentAttempt> findTopByUserAndExamOrderByCreatedAtDesc(User user, Exam exam);

	boolean existsByUserAndExamAndStatus(User user, Exam exam, AttemptStatus onGoing);

	List<StudentAttempt> findByUserAndExam(User user, Exam exam);

}
