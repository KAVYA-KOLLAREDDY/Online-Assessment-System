package com.examApplication.examApplication.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examApplication.examApplication.dto.StudentCertificateDTO;
import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.StudentCertificate;
import com.examApplication.examApplication.entity.auth.User;

public interface StudentCertificateRepository extends JpaRepository<StudentCertificate, Integer> {

	boolean existsByUserAndExam(User user, Exam exam);
    List<StudentCertificate> findByUser_UserId(Integer userId);


}
