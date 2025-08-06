package com.examApplication.examApplication.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.examApplication.examApplication.dto.SubTopicDTO;
import com.examApplication.examApplication.dto.SubTopicDTO.SubtopicInfo;
import com.examApplication.examApplication.entity.SubTopic;
import com.examApplication.examApplication.entity.Subject;
import com.examApplication.examApplication.repository.SubTopicRepository;
import com.examApplication.examApplication.repository.SubjectRepository;

@Service
public class SubTopicService {

    @Autowired
    private SubTopicRepository subtopicRepo;

    @Autowired
    private SubjectRepository subjectRepo;

    public List<SubTopicDTO> getAllSubTopicDTOs() {
        return subtopicRepo.findAll().stream()
            .map(st -> new SubTopicDTO(
                st.getSubject().getSubjectId(),
                st.getSubject().getSubjectName(),
                true,
                List.of(new SubTopicDTO.SubtopicInfo(st.getSubtopicId(), st.getName()))
            )).collect(Collectors.toList());
    }

    public List<SubTopicDTO> createSubtopics(SubTopicDTO dto) {
    Subject subject = subjectRepo.findById(dto.getSubjectId())
            .orElseThrow(() -> new RuntimeException("Subject not found"));

    // ✅ Null check to avoid exception
    if (dto.getSubTopics() == null || dto.getSubTopics().isEmpty()) {
        throw new RuntimeException("Subtopics list cannot be null or empty.");
    }

    List<SubTopicDTO> savedDTOs = dto.getSubTopics().stream().map(subInfo -> {
        SubTopic subtopic = new SubTopic();
        subtopic.setName(subInfo.getName());
        subtopic.setSubject(subject);
        SubTopic saved = subtopicRepo.save(subtopic);

        SubTopicDTO savedDto = new SubTopicDTO();
        savedDto.setSubjectId(subject.getSubjectId());
        savedDto.setSubjectName(subject.getSubjectName());
        savedDto.setAlreadyHasSubtopics(true);

        SubtopicInfo savedInfo = new SubtopicInfo(saved.getSubtopicId(), saved.getName());
        savedDto.setSubTopics(List.of(savedInfo));

        return savedDto;
    }).collect(Collectors.toList());

    return savedDTOs;
}


    public List<SubTopicDTO> getSubTopicDTOsBySubject(Long subjectId) {
        Subject subject = subjectRepo.findById(subjectId)
            .orElseThrow(() -> new RuntimeException("Subject not found"));

        return subtopicRepo.findBySubject(subject).stream()
            .map(st -> new SubTopicDTO(
                subject.getSubjectId(),
                subject.getSubjectName(),
                true,
                List.of(new SubTopicDTO.SubtopicInfo(st.getSubtopicId(), st.getName()))
            )).collect(Collectors.toList());
    }

    public SubTopic getSubtopicByName(String name) {
        return subtopicRepo.findByName(name)
                .orElseThrow(() -> new RuntimeException("Invalid Subtopic Name"));
    }
}
