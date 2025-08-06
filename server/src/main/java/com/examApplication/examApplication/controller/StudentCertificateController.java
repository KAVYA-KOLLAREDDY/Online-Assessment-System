package com.examApplication.examApplication.controller;

import org.springframework.core.io.UrlResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.examApplication.examApplication.dto.StudentCertificateDTO;
import com.examApplication.examApplication.entity.StudentCertificate;
import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.helpers.UserUtils;
import com.examApplication.examApplication.repository.StudentCertificateRepository;
import com.examApplication.examApplication.service.StudentCertificateService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/certificate")
@RequiredArgsConstructor
public class StudentCertificateController {

    private final StudentCertificateService certificateService;
    private final StudentCertificateRepository certificateRepository;

    
    @GetMapping("/my-certificates")
    public ResponseEntity<List<StudentCertificateDTO>> getMyCertificates(User student) {
        User user = UserUtils.getUser(); // adjust depending on your security setup
        List<StudentCertificate> certs = certificateRepository.findByUser_UserId(user.getUserId());
        List<StudentCertificateDTO> dtos = certs.stream()
                .map(cert -> StudentCertificateDTO.builder()
                    .certificateId(cert.getCertificateId())
                    .examTitle(cert.getExam().getTitle())  // Flattening
                    .certificatePath(cert.getCertificatePath())
                    .issuedAt(cert.getIssuedAt())
                    .build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

        @Value("${certificate.output.directory}")
        private String certificateDir; // ✅ Declare it here!

        @GetMapping("/file/{certificateId}")
        public ResponseEntity<Resource> viewOrDownloadCertificate(
                @PathVariable Integer certificateId,
                @RequestParam(defaultValue = "inline") String mode
        ) throws IOException {
            StudentCertificate cert = certificateRepository.findById(certificateId)
                    .orElseThrow(() -> new RuntimeException("Certificate not found"));

            System.out.println("🔥 Reached download controller for ID: " + certificateId);

            // 🟢 Build full path using directory + full filename from DB
            Path filePath = Paths.get(certificateDir, new File(cert.getCertificatePath()).getName());
            File file = filePath.toFile();

            if (!file.exists()) {
                throw new FileNotFoundException("File does not exist: " + filePath);
            }
            System.out.println("📄 Looking for certificate at: " + filePath);

            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));
            String disposition = mode + "; filename=\"" + file.getName() + "\"";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(file.length())
                    .body(resource);
        }
    
}

//@GetMapping
//public ResponseEntity<List<StudentCertificateDTO>> getMyCertificates() {
//
//  User user = UserUtils.getUser();
//
//  List<StudentCertificateDTO> certs = certificateService.getCertificatesForStudent(user);
//
//  return ResponseEntity.ok(certs);
//}
