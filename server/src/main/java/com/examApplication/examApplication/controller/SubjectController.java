package com.examApplication.examApplication.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.examApplication.examApplication.dto.SubTopicDTO;
import com.examApplication.examApplication.dto.SubjectDTO;
import com.examApplication.examApplication.dto.SubjectResponseDTO;
import com.examApplication.examApplication.entity.Subject;
import com.examApplication.examApplication.model.SubjectStatus;
import com.examApplication.examApplication.service.SubjectService;

@RestController
@RequestMapping("/subjects")
public class SubjectController {

    @Autowired
    private SubjectService subjectService;

    @PostMapping("/create")
    public Subject createSubject(@RequestBody SubjectDTO subjectDTO) {
        return subjectService.createSubject(subjectDTO);
    }

    @GetMapping("/all")
    public ResponseEntity<List<SubjectResponseDTO>> getAllSubjects() {
        List<SubjectResponseDTO> responses = subjectService.getAllSubjects();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubjectDTO> getSubject(@PathVariable Long id) {
        return ResponseEntity.ok(subjectService.getSubject(id));
    }

    @GetMapping("/subjects-with-subtopics")
    public List<SubTopicDTO> getAllSubjectsWithSubtopicInfo() {
        return subjectService.getAllSubjectsWithSubtopics();
    }

    @GetMapping("/exam/{examId}")
    public ResponseEntity<SubjectResponseDTO> getSubjectByExam(@PathVariable Integer examId) {
        return ResponseEntity.ok(subjectService.getSubjectByExam(examId));
    }

    @GetMapping("/exam/{examId}/with-subtopics")
    public ResponseEntity<SubTopicDTO> getSubjectWithSubtopicsByExam(@PathVariable Integer examId) {
        SubTopicDTO dto = subjectService.getSubjectWithSubtopicsByExamId(examId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{subjectId}/status")
    public ResponseEntity<String> updateSubjectStatus(
            @PathVariable Long subjectId,
            @RequestParam SubjectStatus status) {

        subjectService.updateSubjectStatus(subjectId, status);
        return ResponseEntity.ok("Subject status updated to " + status.name());
    }
}