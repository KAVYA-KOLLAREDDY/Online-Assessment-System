package com.examApplication.examApplication.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StudentCertificateDTO {
    private Integer certificateId;
    private String examTitle;
    private String certificatePath;
    private LocalDateTime issuedAt;
}
