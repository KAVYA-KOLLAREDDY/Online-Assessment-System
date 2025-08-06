package com.examApplication.examApplication.dto;

import org.springframework.http.HttpStatus;

public record LoginResponseDTO(HttpStatus status, String token) {
	
}
