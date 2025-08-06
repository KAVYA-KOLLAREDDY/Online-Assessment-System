import { Component, inject, OnInit } from '@angular/core';
import { ExamService } from '../../service/exam.service';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { forkJoin, map, switchMap } from 'rxjs';
import { Chart, ChartData, ChartOptions } from 'chart.js';
import ChartDataLabels from 'chartjs-plugin-datalabels';
import { BaseChartDirective } from 'ng2-charts';

Chart.register(ChartDataLabels);

@Component({
  selector: 'app-my-exams',
  imports: [CommonModule,BaseChartDirective],
  templateUrl: './my-exams.component.html',
  styleUrl: './my-exams.component.css'
})

export class MyExamsComponent implements OnInit {
  private examService = inject(ExamService);
  private router = inject(Router);

  passedOrFailedExams: any[] = [];
  loading = true;

  performanceCategoryMap: { [category: string]: string[] } = {
    'Best Performance': [],
    'Midlevel Performance': [],
    'Miserable Performance': []
  };
  // Pagination
  currentPage = 1;
  itemsPerPage = 10;

  difficultyData: any[] = [];
  questionAnalysisData: any[] = [];
  activeResultId: number | null = null;
  activeTab: string = 'Score';
  reportTabs: string[] = ['Score', 'Topic Analysis', 'Difficulty Level', 'Question Analysis'];
  selectedExamReport: any = null;
  Math = Math;


  get paginatedQuestions() {
    const startIndex = (this.currentPage - 1) * this.itemsPerPage;
    return this.questionAnalysisData.slice(startIndex, startIndex + this.itemsPerPage);
  }

  get totalPages() {
    return Math.ceil(this.questionAnalysisData.length / this.itemsPerPage);
  }

  goToPreviousPage(): void {
    if (this.currentPage > 1) this.currentPage--;
  }

  goToNextPage(): void {
    if (this.currentPage < this.totalPages) this.currentPage++;
  }

  goToPage(page: number): void {
    this.currentPage = page;
  }

ngOnInit(): void {
  this.examService.getExams().pipe(
    map((exams: any[]) => {
  const validExams = exams.filter(e => e.examId !== null && e.examId !== undefined);
  const invalidExams = exams.filter(e => !e.examId);
  if (invalidExams.length) {
    console.warn('⚠️ Skipping exams with null examId:', invalidExams);
  }
  return validExams.filter((exam: any) => exam.passed || (!exam.passed && exam.attempts >= 3));
}),

    switchMap((filteredExams) =>
      forkJoin(
        filteredExams.map((exam) =>
          this.examService.getAllAttemptsForExam(exam.examId).pipe(
            map((latestAttempt: any) => {
              return {
                ...exam,
                totalMarks: latestAttempt?.totalMarks ?? 0,
                obtainedMarks: latestAttempt?.obtainedMarks ?? 0,
                passed: latestAttempt?.passed ?? false,
                resultId: latestAttempt?.resultId ?? null
              };
            })
          )
        )
      )
    )
  ).subscribe({
    next: (results) => {
      this.passedOrFailedExams = results;
      this.loading = false;
      console.log('✅ Exams (Passed/Failed) Loaded:', this.passedOrFailedExams);
    },
    error: (err) => {
      console.error('❌ Error loading exams', err);
      this.loading = false;
    }
  });
}

  viewReport(resultId: number): void {
    this.router.navigate(['student/results', resultId]);
  }

  getResultBadge(exam: any): string {
    return exam.passed ? 'badge-pass' : 'badge-fail';
  }

  getResultText(exam: any): string {
    return exam.passed ? 'Passed' : 'Failed';
  }

  toggleReport(exam: any): void {
    if (this.activeResultId === exam.resultId) {
      this.activeResultId = null;
      this.selectedExamReport = null;
      this.difficultyData = [];
    } else {
      this.activeResultId = exam.resultId;
      this.activeTab = 'Score';

      this.examService.getExamResultById(exam.resultId).subscribe({
        next: (data) => {
          this.selectedExamReport = {
            ...exam,
            ...data
          };
          this.updateChartData(data);
          this.loadDifficultyData(exam.resultId);
        },
        error: (err) => {
          console.error('Failed to load detailed report', err);
        }
      });

      this.examService.getQuestionAnalysis(exam.resultId).subscribe({
        next: (data) => {
          console.log('Questions data', data);
          this.questionAnalysisData = data;
        },
        error: (err) => {
          console.error('Error loading question analysis', err);
        }
      });
    }
  }

  loadDifficultyData(resultId: number): void {
    this.examService.getDifficultyAnalysis(resultId).subscribe({
      next: (data) => {
        console.log("Difficulty Analysis Data:", data);
        this.difficultyData = data;
      },
      error: (err) => console.error('Error loading difficulty analysis', err)
    });
  }

  barChartData: ChartData<'bar'> = {
      labels: ['Best Performance', 'Midlevel Performance', 'Miserable Performance'],
      datasets: [{
        label: 'No. of Subtopics',
        data: [0, 0, 0],
        backgroundColor: ['#4CAF50', '#FFC107', '#F44336']
      }]
    };
  
    barChartOptions: ChartOptions<'bar'> = {
      responsive: true,
      scales: {
        x: {
          ticks: {
            font: { weight: 'bold' },
            color: '#2b2f4a'
          },
          title: {
            display: true,
            text: 'Performance Category',
            font: { weight: 'bold' },
            color:'#6a11cb'
          }
        },
        y: {
          ticks: {
            font: { weight: 'bold' },
            color: '#2b2f4a'
          },
          title: {
            display: true,
            text: 'No. of Subtopics',
            font: { weight: 'bold' },
            color:'#6a11cb'
          }
        }
      },
      plugins: {
        legend: { display: false },
        tooltip: {
          // backgroundColor:'#80bdff',
          callbacks: {
            label: (context: any) => {
              const category = context.label;
              const subtopics = this.performanceCategoryMap[category] || [];
              if (subtopics.length === 0) return 'No subtopics';
              const lines = [];
              for (let i = 0; i < subtopics.length; i += 2) {
                lines.push(subtopics.slice(i, i + 2).join(', '));
              }
              return [`Subtopics:`].concat(lines);
            }
          }
        }
      }
    };

updateChartData(data: any): void {
  const topics = data.topics;

  // Reset category map
  this.performanceCategoryMap = {
    'Best Performance': [],
    'Midlevel Performance': [],
    'Miserable Performance': []
  };

  // Categorize subtopics
  topics.forEach((t: any) => {
    if (t.percentage >= 80) {
      this.performanceCategoryMap['Best Performance'].push(t.topic);
    } else if (t.percentage < 10) {
      this.performanceCategoryMap['Miserable Performance'].push(t.topic);
    } else {
      this.performanceCategoryMap['Midlevel Performance'].push(t.topic);
    }
  });

  // Update chart data
  this.barChartData = {
    labels: ['Best Performance', 'Midlevel Performance', 'Miserable Performance'],
    datasets: [{
      label: 'No. of Subtopics',
      data: [
        this.performanceCategoryMap['Best Performance'].length,
        this.performanceCategoryMap['Midlevel Performance'].length,
        this.performanceCategoryMap['Miserable Performance'].length
      ],
      backgroundColor: ['#4CAF50', '#FFC107', '#F44336']
    }]
  };
}
  
}


// import { Component, inject, OnInit } from '@angular/core';
// import { ExamService } from '../../service/exam.service';
// import { Router } from '@angular/router';
// import { CommonModule } from '@angular/common';
// import { forkJoin, map, switchMap } from 'rxjs';

// @Component({
//   selector: 'app-my-exams',
//   imports: [CommonModule],
//   templateUrl: './my-exams.component.html',
//   styleUrl: './my-exams.component.css'
// })
// export class MyExamsComponent implements OnInit {
//   private examService = inject(ExamService);
//   private router = inject(Router);

//   completedExams: any[] = [];
//   loading = true;
//   // Pagination logic
// currentPage = 1;
// itemsPerPage = 10; // Adjust as needed

// difficultyData: any[] = [];
// questionAnalysisData: any[] = [];
// activeResultId: number | null = null;
// activeTab: string = 'Score';
// reportTabs: string[] = ['Score', 'Topic Analysis','Difficulty Level','Question Analysis'];
// selectedExamReport: any = null;


// get paginatedQuestions() {
//   const startIndex = (this.currentPage - 1) * this.itemsPerPage;
//   return this.questionAnalysisData.slice(startIndex, startIndex + this.itemsPerPage);
// }

// get totalPages() {
//   return Math.ceil(this.questionAnalysisData.length / this.itemsPerPage);
// }
// goToPreviousPage(): void {
//   if (this.currentPage > 1) this.currentPage--;
// }

// goToNextPage(): void {
//   if (this.currentPage < this.totalPages) this.currentPage++;
// }

// goToPage(page: number): void {
//   this.currentPage = page;
// }


//  ngOnInit(): void {
//   this.examService.getExams().pipe(
//     map((exams: any[]) =>
//       exams.filter((exam: any) => exam.passed || (!exam.passed && exam.attempts >= 3))
//     ),
//     switchMap((completedExams) =>
//       forkJoin(
//         completedExams.map((exam) =>
//           this.examService.getAllAttemptsForExam(exam.examId).pipe(
//             map((latestAttempt: any) => {
//               return {
//                 ...exam,
//                 totalMarks: latestAttempt?.totalMarks ?? 0,
//                 obtainedMarks: latestAttempt?.obtainedMarks ?? 0,
//                 passed: latestAttempt?.passed ?? false,
//                 resultId: latestAttempt?.resultId ?? null
//               };
//             })
//           )
//         )
//       )
//     )
//   ).subscribe({
//     next: (finalResults) => {
//       this.completedExams = finalResults;
//       this.loading = false; // ✅ Don't forget this!
//       console.log('✅ Completed Exams Loaded:', this.completedExams);
//     },
//     error: (err) => {
//       console.error('❌ Error loading exams', err);
//       this.loading = false; // ✅ Also stop loading on error
//     }
//   });
// }

//   viewReport(resultId: number): void {
//     this.router.navigate(['student/results', resultId]);
//   }

//   getResultBadge(exam: any): string {
//     return exam.passed ? 'badge-pass' : 'badge-fail';
//   }

//   getResultText(exam: any): string {
//     return exam.passed ? 'Pass' : 'Fail';
//   }

// toggleReport(exam: any): void {
//   if (this.activeResultId === exam.resultId) {
//     this.activeResultId = null;
//     this.selectedExamReport = null;
//     this.difficultyData = []; // 👈 clear old data
//   } else {
//     this.activeResultId = exam.resultId;
//     this.activeTab = 'Score';

//     this.examService.getExamResultById(exam.resultId).subscribe({
//       next: (data) => {
//         this.selectedExamReport = {
//           ...exam,
//           ...data
//         };

//         this.loadDifficultyData(exam.resultId);
//       },
//       error: (err) => {
//         console.error('Failed to load detailed report', err);
//       }
//     });
//   }
//   this.examService.getQuestionAnalysis(exam.resultId).subscribe({
//   next: (data) => {
//     console.log('Questions data',data);
//     this.questionAnalysisData = data;
//   },
//   error: (err) => {
//     console.error('Error loading question analysis', err);
//   }
// });

// }

// loadDifficultyData(resultId: number): void {
//   this.examService.getDifficultyAnalysis(resultId).subscribe({
//     next: (data) => {
//       console.log("Distributed Data:",data);
//       this.difficultyData = data;
//       console.log(this.difficultyData);
//     },
//     error: (err) => console.error('Error loading difficulty analysis', err)
//   });
// }


// }