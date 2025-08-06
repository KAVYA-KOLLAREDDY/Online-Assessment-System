package com.examApplication.examApplication.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.examApplication.examApplication.dto.SubTopicDTO;
import com.examApplication.examApplication.dto.SubjectDTO;
import com.examApplication.examApplication.dto.SubjectResponseDTO;
import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.SubTopic;
import com.examApplication.examApplication.entity.Subject;
import com.examApplication.examApplication.helpers.UserUtils;
import com.examApplication.examApplication.model.SubjectStatus;
import com.examApplication.examApplication.repository.ExamRepository;
import com.examApplication.examApplication.repository.SubTopicRepository;
import com.examApplication.examApplication.repository.SubjectRepository;

@Service
public class SubjectService {

    @Autowired
    private SubjectRepository subjectRepository;

    @Autowired
    private ExamRepository examRepository;

    @Autowired
    private SubTopicRepository subTopicRepository;

    public Subject createSubject(Subject subject) {
        return subjectRepository.save(subject);
    }

    public Subject createSubject(SubjectDTO dto) {
        Subject subject = new Subject();
        subject.setSubjectName(dto.getSubjectName());
        subject.setDescription(dto.getDescription());
        subject.setCreatedBy(UserUtils.getUser());

        // subject.setCreatedBy(1);
        subject.setStatus(SubjectStatus.ACTIVE);

        return subjectRepository.save(subject);
    }

    public List<SubjectResponseDTO> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(subject -> SubjectResponseDTO
                        .builder()
                        .subjectId(subject.getSubjectId())
                        .subjectName(subject.getSubjectName())
                        .description(subject.getDescription())
                        .createdDate(subject.getCreatedDate())
                        .status(subject.getStatus().name())
                        .build())
                .toList();
    }

    public SubjectDTO getSubject(Long id) {
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subject not found!"));
        
        return new SubjectDTO(
            subject.getSubjectId(),
            subject.getSubjectName(),
            subject.getDescription(),
            subject.getStatus().toString()
        );
    }

   public List<SubTopicDTO> getAllSubjectsWithSubtopics() {
    List<Subject> subjects = subjectRepository.findAll();

    return subjects.stream().map(subject -> {
        List<SubTopicDTO.SubtopicInfo> subtopicList = subTopicRepository.findBySubject(subject)
                .stream()
                .map(sub -> new SubTopicDTO.SubtopicInfo(sub.getSubtopicId(), sub.getName()))
                .collect(Collectors.toList());

        SubTopicDTO dto = new SubTopicDTO();
        dto.setSubjectId(subject.getSubjectId());
        dto.setSubjectName(subject.getSubjectName());
        dto.setSubTopics(subtopicList);
        dto.setAlreadyHasSubtopics(!subtopicList.isEmpty());
        return dto;
    }).collect(Collectors.toList());
}

    public SubjectResponseDTO getSubjectByExam(Integer examId) {
    Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + examId));

    Subject subject = exam.getSubject();

    return SubjectResponseDTO.builder()
            .subjectId(subject.getSubjectId())
            .subjectName(subject.getSubjectName())
            .description(subject.getDescription())
            .createdDate(subject.getCreatedDate())
            .status(subject.getStatus().name())
            .build();
}
public SubTopicDTO getSubjectWithSubtopicsByExamId(Integer examId) {
    Exam exam = examRepository.findById(examId)
            .orElseThrow(() -> new RuntimeException("Exam not found with ID: " + examId));

    Subject subject = exam.getSubject();
    if (subject == null) {
        throw new RuntimeException("No subject linked with this exam.");
    }

    List<SubTopicDTO.SubtopicInfo> subtopicList = subTopicRepository.findBySubject(subject)
            .stream()
            .map(sub -> new SubTopicDTO.SubtopicInfo(sub.getSubtopicId(), sub.getName()))
            .collect(Collectors.toList());

    SubTopicDTO dto = new SubTopicDTO();
    dto.setSubjectId(subject.getSubjectId());
    dto.setSubjectName(subject.getSubjectName());
    dto.setSubTopics(subtopicList);
    dto.setAlreadyHasSubtopics(!subtopicList.isEmpty());

    return dto;
}

    public void updateSubjectStatus(Long subjectId, SubjectStatus status) {
    Subject subject = subjectRepository.findById(subjectId)
        .orElseThrow(() -> new RuntimeException("Subject not found"));

    subject.setStatus(status);
    subjectRepository.save(subject);
    }

}