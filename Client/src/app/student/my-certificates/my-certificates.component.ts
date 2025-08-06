import { Component, inject, OnInit } from '@angular/core';
import { ExamService } from '../../service/exam.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-my-certificates',
  imports: [CommonModule],
  templateUrl: './my-certificates.component.html',
  styleUrl: './my-certificates.component.css'
})
export class MyCertificatesComponent implements OnInit {
  private examService = inject(ExamService);

  certificates: any[] = [];
  loading = true;
  errorMessage: string | null = null;

  ngOnInit(): void {
    this.examService.getStudentCertificates().subscribe({
      next: (data) => {
        this.certificates = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Error loading certificates:', err);
        this.errorMessage = 'Failed to load certificates.';
        this.loading = false;
      }
    });
  }

  // ✅ Open view URL in new tab
  viewCertificate(certId: number) {
    const viewUrl = this.examService.viewCertificate(certId);
    window.open(viewUrl, '_blank');
  }

  // ✅ Trigger download
  downloadCertificate(certId: number, examTitle: string) {
    this.examService.downloadCertificate(certId).subscribe(blob => {
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = `Certificate_${examTitle.replace(/ /g, '_')}.pdf`;
      link.click();
      window.URL.revokeObjectURL(url);
    });
  }
}
