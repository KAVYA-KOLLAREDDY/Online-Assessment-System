package com.examApplication.examApplication.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examApplication.examApplication.entity.SubTopic;
import com.examApplication.examApplication.entity.Subject;

public interface SubTopicRepository extends JpaRepository<SubTopic, Long> {
	List<SubTopic> findBySubject(Subject subject);
	Optional<SubTopic> findByName(String name);
	
	Boolean existsByNameAndSubject(String name, Subject subject);
}
