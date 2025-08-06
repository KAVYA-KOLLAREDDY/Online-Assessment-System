package com.examApplication.examApplication.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examApplication.examApplication.dto.ExamDTO;
import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.auth.User;


public interface ExamRepository extends JpaRepository<Exam, Integer>{
        List<Exam> findByCreatedBy(User user);

}
