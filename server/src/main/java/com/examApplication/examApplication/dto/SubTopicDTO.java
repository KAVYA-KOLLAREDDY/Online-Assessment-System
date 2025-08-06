package com.examApplication.examApplication.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubTopicDTO {
    private Long subjectId;
    private String subjectName;
    private boolean alreadyHasSubtopics;
    private List<SubtopicInfo> subTopics; 

    @Getter @Setter
    public static class SubtopicInfo {
        private Long subtopicId;
        private String name;

        public SubtopicInfo(Long id, String name) {
            this.subtopicId = id;
            this.name = name;
        }
    }

}
