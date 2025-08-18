package com.examApplication.examApplication.service;

import java.awt.Color;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.examApplication.examApplication.dto.StudentCertificateDTO;
import com.examApplication.examApplication.entity.Exam;
import com.examApplication.examApplication.entity.ExamResult;
import com.examApplication.examApplication.entity.StudentCertificate;
import com.examApplication.examApplication.entity.auth.User;
import com.examApplication.examApplication.model.ExamType;
import com.examApplication.examApplication.repository.ExamResultRepository;
import com.examApplication.examApplication.repository.StudentCertificateRepository;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentCertificateService {
	@Value("${certificate.template.path}")
    private String templatePath;

    @Value("${certificate.output.directory}")
    private String outputDirectory;

    private final StudentCertificateRepository certificateRepository;
    private final JavaMailSender mailSender;
    private final ExamResultRepository examResultRepository;

    public StudentCertificate generateAndStoreCertificate(User user, Exam exam) {
        try {
            String fileName = "Certificate_" + user.getName().replaceAll(" ", "_")
                             + "_" + exam.getTitle().replaceAll(" ", "_") + ".pdf";

            Path fullPath = Paths.get(outputDirectory, fileName);
            String fullOutputPath = fullPath.toAbsolutePath().toString();

            File template = ResourceUtils.getFile("classpath:" + templatePath);
            PDDocument document = PDDocument.load(template);
            PDPage page = document.getPage(0);

            PDPageContentStream contentStream = new PDPageContentStream(
                    document, page, PDPageContentStream.AppendMode.APPEND, true, true);

            Color textColor = Color.BLACK;
            PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;
            PDType1Font fontRegular = PDType1Font.HELVETICA;
            int fontSize = 16;
            int titleFontSize = 18;
            float pageWidth = page.getMediaBox().getWidth();

            // 🧑 Student Name (centered)
            String studentName = user.getName();
            float nameWidth = fontBold.getStringWidth(studentName) / 1000 * titleFontSize;
            contentStream.setNonStrokingColor(textColor);
            contentStream.setFont(fontBold, titleFontSize);
            contentStream.beginText();
            contentStream.newLineAtOffset((pageWidth - nameWidth) / 2, 340);
            contentStream.showText(studentName);
            contentStream.endText();

            // 📝 ExamResult (for percentage if needed)
            // ExamResult result = examResultRepository.findByExam_ExamIdAndUser_UserId(
            //     exam.getExamId(), user.getUserId());
Optional<ExamResult> resultOpt = examResultRepository
    .findTopByExam_ExamIdAndUser_UserIdAndPassedIsTrueOrderByCompletedAtDesc(
        exam.getExamId(), user.getUserId());

if (resultOpt.isEmpty()) {
    throw new RuntimeException("No passed exam attempt found for certificate generation.");
}

ExamResult result = resultOpt.get();

            // 👇 Text Blocks Based on ExamType
            String middleLine;
            String extraLine = "";
            String footerLine = "";
            boolean showCourseTitle = true;
            float dateY = 184;

            if (exam.getExamType() == ExamType.Programming) {
                middleLine = "has completed the necessary courses of study and passed the My3Tech " + exam.getTitle() + " exam";
                extraLine = "and is hereby declared as";
                footerLine = "with fundamental knowledge of programming using " + exam.getTitle() + ".";
            } else {
                double percentage = result != null ? result.getPercentage() : 0.0;
                middleLine = "has completed the necessary courses of study and passed the My3Tech " + exam.getTitle() + " exam";
                extraLine = "with a percentage of " + String.format("%.1f", percentage) + "%";
                showCourseTitle = false;
                dateY = 200; // move date upward
            }

            // 🧾 Middle line (centered)
            float midWidth = fontRegular.getStringWidth(middleLine) / 1000 * fontSize;
            contentStream.setFont(fontRegular, fontSize);
            contentStream.beginText();
            contentStream.newLineAtOffset((pageWidth - midWidth) / 2, 308);
            contentStream.showText(middleLine);
            contentStream.endText();

            // ✏️ Extra line below middleLine
            if (!extraLine.isEmpty()) {
                float extraWidth = fontRegular.getStringWidth(extraLine) / 1000 * fontSize;
                contentStream.beginText();
                contentStream.newLineAtOffset((pageWidth - extraWidth) / 2, 286);
                contentStream.showText(extraLine);
                contentStream.endText();
            }

            // ✅ Programming-only: Course Title
            if (showCourseTitle) {
                String courseTitle = "Certified " + exam.getTitle() + " Developer";
                float courseWidth = fontBold.getStringWidth(courseTitle) / 1000 * fontSize;
                contentStream.setFont(fontBold, fontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset((pageWidth - courseWidth) / 2, 252);
                contentStream.showText(courseTitle);
                contentStream.endText();
            }

            // ✅ Footer line (e.g., programming knowledge)
            if (!footerLine.isEmpty()) {
                float footerWidth = fontRegular.getStringWidth(footerLine) / 1000 * fontSize;
                contentStream.setFont(fontRegular, fontSize);
                contentStream.beginText();
                contentStream.newLineAtOffset((pageWidth - footerWidth) / 2, 220);
                contentStream.showText(footerLine);
                contentStream.endText();
            }

            // 📅 Issue Date (centered)
            String issueDate = "Issued " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            float dateWidth = fontRegular.getStringWidth(issueDate) / 1000 * titleFontSize;
            contentStream.setFont(fontRegular, titleFontSize);
            contentStream.beginText();
            contentStream.newLineAtOffset((pageWidth - dateWidth) / 2, dateY);
            contentStream.showText(issueDate);
            contentStream.endText();

            contentStream.close();

            File dir = new File(outputDirectory);
            if (!dir.exists()) dir.mkdirs();

            document.save(fullPath.toFile());
            document.close();

            StudentCertificate certificate = StudentCertificate.builder()
                    .user(user)
                    .exam(exam)
                    .certificatePath("certificates/" + fileName)
                    .issuedAt(LocalDateTime.now())
                    .build();

            StudentCertificate saved = certificateRepository.save(certificate);
            sendCertificateEmail(user.getEmail(), fullOutputPath);

            return saved;

        } catch (Exception e) {
            throw new RuntimeException("❌ Failed to generate certificate: " + e.getMessage(), e);
        }
    }

    private void sendCertificateEmail(String recipientEmail, String attachmentPath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(recipientEmail);
            helper.setSubject("🎉 Congratulations! You Have Earned a Certificate");
            helper.setText("Dear Student,\n\nCongratulations on passing the exam! Please find your certificate attached.\n\nBest Regards,\nMy3Tech");

            FileSystemResource file = new FileSystemResource(new File(attachmentPath));
            helper.addAttachment("Certificate.pdf", file);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send certificate email: " + e.getMessage());
        }
    }

}
//  public StudentCertificate generateAndStoreCertificate(User user, Exam exam) {
//  try {
//      // 📝 Generate filename
//      String fileName = "Certificate_" + user.getName().replaceAll(" ", "_")
//                       + "_" + exam.getTitle().replaceAll(" ", "_") + ".pdf";
//
//      // 🛣 Build full output path
//      Path fullPath = Paths.get(outputDirectory, fileName);
//      String fullOutputPath = fullPath.toAbsolutePath().toString();
//
//      System.out.println("Attempting to save certificate to: " + fullOutputPath);
//      // 📄 Load PDF template from classpath
//      File template = ResourceUtils.getFile("classpath:" + templatePath);
//      PDDocument document = PDDocument.load(template);
//
//      // ✏️ Add dynamic text
//      PDPage page = document.getPage(0);
//      PDPageContentStream contentStream = new PDPageContentStream(document, page,
//              PDPageContentStream.AppendMode.APPEND, true, true);
//
//      contentStream.setNonStrokingColor(Color.BLACK);
//      contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
//
//      // 🧑 Student Name
//      contentStream.beginText();
//      contentStream.newLineAtOffset(330, 340);
//      contentStream.showText(user.getName());
//      contentStream.endText();
//
//      // 🎓 Course Title
//      contentStream.beginText();
//      contentStream.newLineAtOffset(325, 252);
//      contentStream.showText("Certified " + exam.getTitle() + " Developer");
//      contentStream.endText();
//
//      // 📅 Issue Date
//      String issueDate = "Issued " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
//      contentStream.beginText();
//      contentStream.newLineAtOffset(336, 184); //tx,ty
//      contentStream.showText(issueDate);
//      contentStream.endText();
//
//      contentStream.close();
//
//      // 💾 Ensure directory exists
//      File dir = new File(outputDirectory);
//      if (!dir.exists()) {
//          dir.mkdirs();
//          System.out.println("Created directory: " + outputDirectory);
//      }
//
//      // 📤 Save certificate to file
//      document.save(fullPath.toFile());
//      document.close();
//
//      System.out.println("✅ Certificate saved to: " + fullOutputPath);
//
//      // 🧾 Save certificate record to DB
//      StudentCertificate certificate = StudentCertificate.builder()
//              .user(user)
//              .exam(exam)
//              .certificatePath("certificates/" + fileName)  // ✅ relative path for frontend access
//              .issuedAt(LocalDateTime.now())
//              .build();
//
//      StudentCertificate saved = certificateRepository.save(certificate);
//      File savedFile = fullPath.toFile();
//      if (!savedFile.exists()) {
//          throw new RuntimeException("File was not saved or is inaccessible: " + fullOutputPath);
//      }
//      if (!savedFile.canRead()) {
//          throw new RuntimeException("File is not readable: " + fullOutputPath);
//      }
//      // ✉️ Send certificate as email attachment
//      sendCertificateEmail(user.getEmail(), fullOutputPath);
//
//      return saved;
//
//  } catch (Exception e) {
//      throw new RuntimeException("Failed to generate certificate: " + e.getMessage(), e);
//  }
//}
    
//    public List<StudentCertificateDTO> getCertificatesForStudent(User student) {
//        String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString(); // http://localhost:8081
//
//        List<StudentCertificate> certs = certificateRepository.findByUser_UserId(student.getUserId());
//
//        return certs.stream()
//                .map(cert -> {
//                    String relativePath = cert.getCertificatePath(); // e.g., certificates/Certificate_Venkat_Ramayya_Java_Exam.pdf
//                    String fullPath = baseUrl + "/" + relativePath;   // 👉 http://localhost:8081/certificates/...
//                    return new StudentCertificateDTO(
//                            cert.getCertificateId(),
//                            cert.getExam().getTitle(),
//                            fullPath,
//                            cert.getIssuedAt()
//                    );
//                })
//                .toList();
//    }
