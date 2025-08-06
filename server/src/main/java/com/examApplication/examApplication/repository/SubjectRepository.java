package com.examApplication.examApplication.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {

}