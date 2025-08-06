package com.examApplication.examApplication.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAccessRequestDTO {
	private Integer userId;
	private boolean active;
	private boolean locked;

}
