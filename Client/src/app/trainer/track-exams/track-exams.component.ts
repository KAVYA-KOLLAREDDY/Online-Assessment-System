import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { CommonModule, NgClass } from '@angular/common';
import { ReactiveFormsModule } from '@angular/forms';
import { ExamResult } from '../../models/examResult.model';
import { ExamService } from '../../service/exam.service';
import { handleResposne } from '../../utils/handle-response.utils';
import { LoggingService } from '../../service/logging.service';
import { injectNgGlyphsConfig } from '@ng-icons/core';

@Component({
  selector: 'app-track-exams',
  imports: [CommonModule, ReactiveFormsModule, NgClass],
  templateUrl: './track-exams.component.html',
  styleUrl: './track-exams.component.css',
})
export class TrackExamsComponent implements OnInit {
  examResults = signal<any[]>([]);
  pageSize = signal(5);
  currentPage = signal(1);

  // 🔍 Search & Filter Signals
  searchTerm = signal('');
  selectedStudent = signal('');
  selectedExam = signal('');

  private examService = inject(ExamService);
  private loggingService = inject(LoggingService);

  constructor() {
    // Optional: Reset pagination on filter change
    effect(() => {
      this.currentPage.set(1);
    });
  }

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

  // 🔄 Get unique student and exam names
  get studentNames(): string[] {
    return [...new Set(this.examResults().map(e => e.studentName).filter(Boolean))];
  }

  get examNames(): string[] {
    return [...new Set(this.examResults().map(e => e.examName).filter(Boolean))];
  }

  // 🧠 Combined filter logic
  filteredResults = computed(() => {
    const term = this.searchTerm().toLowerCase();
    const student = this.selectedStudent();
    const exam = this.selectedExam();

    return this.examResults().filter(r => {
      const matchesSearch =
        !term ||
        (r.studentName && r.studentName.toLowerCase().includes(term)) ||
        (r.examName && r.examName.toLowerCase().includes(term)) ||
        (r.isQualified !== undefined && (r.isQualified ? 'passed' : 'failed').includes(term));

      const matchesStudent = !student || r.studentName === student;
      const matchesExam = !exam || r.examName === exam;

      return matchesSearch && matchesStudent && matchesExam;
    });
  });

  // Paginate after filtering
  paginatedResults = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize();
    const end = start + this.pageSize();
    return this.filteredResults().slice(start, end);
  });

  totalPages = computed(() => Math.ceil(this.filteredResults().length / this.pageSize()));

  // Pagination Controls
  nextPage() {
    if (this.currentPage() < this.totalPages()) {
      this.currentPage.update(p => p + 1);
    }
  }

  prevPage() {
    if (this.currentPage() > 1) {
      this.currentPage.update(p => p - 1);
    }
  }

  getStartItem() {
    return (this.currentPage() - 1) * this.pageSize() + 1;
  }

  getEndItem() {
    const end = this.currentPage() * this.pageSize();
    return end > this.filteredResults().length ? this.filteredResults().length : end;
  }
}

