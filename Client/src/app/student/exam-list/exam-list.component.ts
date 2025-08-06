import { Component, inject, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { ExamService } from '../../service/exam.service';
import { Exam } from '../../models/exam.model';

@Component({
  selector: 'app-exam-list',
  imports: [CommonModule],
  templateUrl: './exam-list.component.html',
  styleUrl: './exam-list.component.css',
})
export class ExamListComponent implements OnInit {

  exams: any[] = [];
  private examService = inject(ExamService);
  private router = inject(Router);

  ongoingExams: any[] = [];
  upcomingExams: any[] = [];
  missedExams: any[] = [];
  completedPassedExams: any[] = [];
  completedFailedExams: any[] = [];

  ngOnInit(): void {
    this.examService.getExams().subscribe({
      next: (data) => {
        console.log("Exam Data:",data);
        this.exams = data;
        const now = new Date();

        this.ongoingExams = data.filter(e => {
          const start = new Date(e.startTime);
          const end = new Date(e.endTime);
          return now >= start && now <= end && !e.passed && e.attempts < 3;
        });

        this.upcomingExams = data.filter(e => {
          const start = new Date(e.startTime);
          return now < start;
        });

        this.missedExams = data.filter(e => {
          const end = new Date(e.endTime);
          return now > end && !e.passed;
        });

        this.completedPassedExams = data.filter(e => e.passed === true);

        this.completedFailedExams = data.filter(e => e.attempts >= 3 && !e.passed);
      }
    });
  }

  startExam(exams: any): void {
    if (exams.passed) {
      alert("✅ You have already passed this exam.");
      return;
    }

    if (exams.attempts >= 3 && !exams.passed) {
      alert("❌ You have failed the exam and cannot take it again.");
      return;
    }

    const elem = document.documentElement;

    if (elem.requestFullscreen) {
      elem.requestFullscreen().then(() => {
        this.examService.setCurrentExam(exams);
        this.router.navigate(['student/guidelines', exams.examId]);
      }).catch(err => {
        alert("⚠️ Fullscreen mode is required to start the exam.");
        console.error("Fullscreen error:", err);
      });
    } else {
      alert("❌ Your browser does not support fullscreen mode.");
    }
  }

  getButtonLabel(exams: any): string {
    const today = new Date();
    const startDate = new Date(exams.startTime);
    const endDate = new Date(exams.endTime);

    if (today < startDate) {
      return 'Not Available Currently';
    }

    if (today > endDate && !exams.passed) {
      return 'Expired';
    }

    if (exams.passed) {
      return 'Passed'; // ✅ Changed from "Completed" to "Passed"
    }

    if (exams.attempts === 0) {
      return 'Start Exam';
    }

    if (exams.attempts < 3 && !exams.passed) {
      return `Retake (Attempts Left: ${3 - exams.attempts})`;
    }

    return 'Failed';
  }

  getButtonClass(exams: any): string {
    const today = new Date();
    const startDate = new Date(exams.startTime);
    const endDate = new Date(exams.endTime);

    if (today < startDate) return 'btn-not-started';
    if (today > endDate && !exams.passed) return 'btn-expired';

    if (exams.passed) return 'btn-passed'; // ✅ rename class to reflect label
    if (exams.attempts === 0) return 'btn-start';
    if (exams.attempts < 3 && !exams.passed) return 'btn-retake';

    return 'btn-failed';
  }

  isButtonDisabled(exams: any): boolean {
    const today = new Date();
    const startDate = new Date(exams.startTime);
    const endDate = new Date(exams.endTime);

    return today < startDate || today > endDate || exams.passed || (exams.attempts >= 3 && !exams.passed);
  }

  navigateResult(id: number) {
    this.router.navigate([`student/results/${id}`]);
  }
}

// import { Component, inject, OnInit } from '@angular/core';
// import { Router } from '@angular/router';
// import { CommonModule } from '@angular/common';
// import { ExamService } from '../../service/exam.service';
// import { Exam } from '../../models/exam.model';
// import { LoggingService } from '../../service/logging.service';
// import { handleResposne } from '../../utils/handle-response.utils';

// @Component({
//   selector: 'app-exam-list',
//   imports: [CommonModule],
//   templateUrl: './exam-list.component.html',
//   styleUrl: './exam-list.component.css',
// })
// export class ExamListComponent implements OnInit {

//   exams: any[] = [];
//   private examService = inject(ExamService);
//   private router = inject(Router);

//   ongoingExams: any[] = [];
// upcomingExams: any[] = [];
// missedExams: any[] = [];
// completedPassedExams: any[] = [];
// completedFailedExams: any[] = [];

// ngOnInit(): void {
//   this.examService.getExams().subscribe({
//     next: (data) => {
//       this.exams = data;
//       const now = new Date();

//       this.ongoingExams = data.filter(e => {
//         const start = new Date(e.startTime);
//         const end = new Date(e.endTime);
//         return now >= start && now <= end && !e.passed && e.attempts < 3;
//       });

//       this.upcomingExams = data.filter(e => {
//         const start = new Date(e.startTime);
//         return now < start;
//       });

//       this.missedExams = data.filter(e => {
//         const end = new Date(e.endTime);
//         return now > end && !e.passed;
//       });

//       this.completedPassedExams = data.filter(e => e.passed === true);

//       this.completedFailedExams = data.filter(e => e.attempts >= 3 && !e.passed);
//     }
//   });
// }
//   // startExam(exam: Exam) {
//   //   this.examService.setCurrentExam(exam);
//   //   this.router.navigate(['student/exam', exam.examId]);
//   // }
//   startExam(exams: any): void {
//     if (exams.passed) {
//       alert("✅ You have already completed this exam.");
//       return;
//     }

//     if (exams.attempts >= 3 && !exams.passed) {
//       alert("❌ You have failed the exam and cannot take it again.");
//       return;
//     }

//     const elem = document.documentElement;
    
//     // Request Fullscreen
//     if (elem.requestFullscreen) {
//      elem.requestFullscreen().then(() => {
//        this.examService.setCurrentExam(exams);
//        this.router.navigate(['student/exam', exams.examId]);
//       }).catch(err => {
//        alert("⚠️ Fullscreen mode is required to start the exam.");
//         console.error("Fullscreen error:", err);
//       });
//     } 
//     else {
//         alert("❌ Your browser does not support fullscreen mode.");
//     }
//     // this.examService.setCurrentExam(exams);
//     // this.router.navigate(['student/exam', exams.examId], {replaceUrl: true});
//   }

//   getButtonLabel(exams: any): string {
//     const today = new Date();
//     const startDate = new Date(exams.startTime);
//     const endDate = new Date(exams.endTime);

//     if(today < startDate){
//       return 'Not Avaiable Currently';
//     }

//     if(today > endDate){
//       return 'Expired';
//     }

//     if (exams.passed) {
//       return 'Completed';
//     } else if (exams.attempts === 0) {
//       return 'Start Exam';
//     } else if (exams.attempts < 3 && !exams.passed) {
//       return 'Retake (Attempts Left: ${3 - exams.attempts})';
//     } else {
//       return 'Failed';
//     }
//   }

//   getButtonClass(exams: any): string {
//     const today = new Date();
//     const startDate = new Date(exams.startTime);
//     const endDate = new Date(exams.endTime);

//     if(today < startDate){
//       return 'btn-not-started';
//     }
//     if(today > endDate){
//       return 'btn-expired';
//     }

//     if (exams.passed) return 'btn-completed';
//     if (exams.attempts === 0) return 'btn-start';
//     if (exams.attempts < 3 && !exams.passed) return 'btn-retake';
//     return 'btn-failed';
//   }
  
//   isButtonDisabled(exams: any): boolean {
//     const today = new Date();
//     const startDate = new Date(exams.startTime);
//     const endDate = new Date(exams.endTime);

//     if (today < startDate || today > endDate) return true;
//     return exams.passed || (exams.attempts >= 3 && !exams.passed);
//   }

//   navigateResult(id: number) {
//     this.router.navigate([`student/results/${id}`])
//   }

// }


