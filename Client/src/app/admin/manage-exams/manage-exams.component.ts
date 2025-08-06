import { Component, inject, OnInit } from '@angular/core';
import { Exam } from '../../models/exam.model';
import { ExamService } from '../../service/exam.service';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { Question } from '../../models/question.model';
import { handleResposne } from '../../utils/handle-response.utils';
import { LoggingService } from '../../service/logging.service';

@Component({
  selector: 'app-manage-exams',
  imports: [CommonModule],
  templateUrl: './manage-exams.component.html',
  styleUrl: './manage-exams.component.css',
})
export class ManageExamsComponent implements OnInit {
  exams: Exam[] = [];
  // Selected exam details to display in modal
  selectedExam: Exam | null = null;
  examQuestions: Question[] = [];
  showModal: boolean = false;
  private loggingService = inject(LoggingService);

  constructor(private examService: ExamService, private router: Router) {}

  ngOnInit(): void {
    this.loadExams();
  }

  // Load exams from the backend
  loadExams(): void {
    this.examService.getExams().subscribe(
      handleResposne(this.loggingService, (data) => {
        this.exams = data;
      })
    );
  }

  viewExam(exam: Exam): void {
    this.selectedExam = exam;

    this.examService.getAllQuestions(exam.examId).subscribe(
      handleResposne(this.loggingService, (questions) => {
        console.log(questions);
        this.examQuestions = questions;
        this.showModal = true;
      })
    );
  }

  closeModal(): void {
    this.showModal = false;
    this.selectedExam = null;
    this.examQuestions = [];
  }

  deleteExam(examId: number): void {
    if (confirm('Are you sure you want to delete this exam?')) {
      this.examService.deleteExam(examId).subscribe(
        handleResposne(this.loggingService, () => {
          this.loggingService.onSuccess('Exam Deleted Successfully');
          this.loadExams();
        })
      );
    }
  }
}
