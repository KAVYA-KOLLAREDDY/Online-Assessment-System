import { Component, inject, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ExamService } from '../../service/exam.service';

@Component({
  selector: 'app-exam-guidelines',
  imports: [],
  templateUrl: './exam-guidelines.component.html',
  styleUrl: './exam-guidelines.component.css'
})
export class ExamGuidelinesComponent implements OnInit {
  examId!: number;
  router = inject(Router);
  route = inject(ActivatedRoute);
  examService = inject(ExamService);

  ngOnInit(): void {
    const paramId = this.route.snapshot.paramMap.get('id');
    if (!paramId || isNaN(+paramId)) {
      alert("Invalid exam ID");
      this.router.navigate(['/student/exams']);
      return;
    }
    this.examId = +paramId;
  }

  proceedToExam(): void {
    document.documentElement.requestFullscreen().then(() => {
      this.router.navigate(['/student/exam', this.examId]);
    }).catch(() => {
      alert("Please allow fullscreen mode to begin the exam.");
    });
  }
}