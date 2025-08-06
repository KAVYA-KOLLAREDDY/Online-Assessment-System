import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ExamResult } from '../../models/examResult.model';
import { ExamService } from '../../service/exam.service';
import { handleResposne } from '../../utils/handle-response.utils';
import { LoggingService } from '../../service/logging.service';

@Component({
  selector: 'app-track-exams',
  imports: [CommonModule, ReactiveFormsModule, NgClass],
  templateUrl: './track-exams.component.html',
  styleUrl: './track-exams.component.css',
})
export class TrackExamsComponent implements OnInit {
  examResults = signal<any[]>([]);
  private examService = inject(ExamService);

  private loggingService = inject(LoggingService);

  ngOnInit() {
    this.fetchExamResults();
  }

  fetchExamResults() {
    this.examService.getAllExamResults().subscribe(
      handleResposne(this.loggingService, (results) => {
        this.examResults.set(results);
      })
    );
  }
}
