package com.examApplication.examApplication.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.examApplication.examApplication.dto.SubTopicDTO;
import com.examApplication.examApplication.entity.SubTopic;
import com.examApplication.examApplication.service.SubTopicService;

@RestController
@RequestMapping("/subtopics")
public class SubTopicController {

    @Autowired
    private SubTopicService subtopicService;
    
    @GetMapping
    public List<SubTopicDTO> getAllSubTopics(){
    	return subtopicService.getAllSubTopicDTOs();
    }

    @PostMapping
    public List<SubTopicDTO> create(@RequestBody SubTopicDTO dto) {
        return subtopicService.createSubtopics(dto);
    }

    @GetMapping("/subject/{subjectId}")
    public List<SubTopicDTO> getBySubject(@PathVariable Long subjectId) {
        return subtopicService.getSubTopicDTOsBySubject(subjectId);
    }

}
