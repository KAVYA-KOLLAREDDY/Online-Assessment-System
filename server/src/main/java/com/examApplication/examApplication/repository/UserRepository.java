package com.examApplication.examApplication.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.model.Status;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmailAndPassword(String email, String password);
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    List<User> findByStatus(Status status);

}