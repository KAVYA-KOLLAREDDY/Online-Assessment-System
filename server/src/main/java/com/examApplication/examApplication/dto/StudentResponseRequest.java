package com.examApplication.examApplication.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponseRequest {
	private Integer question;
	private List<Integer> selectedOption;
}
