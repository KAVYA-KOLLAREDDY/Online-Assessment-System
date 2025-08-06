import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ExamService } from '../../service/exam.service';
import { LoggingService } from '../../service/logging.service';

@Component({
  selector: 'app-exam-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './exam-management.component.html',
  styleUrls: ['./exam-management.component.css']
})
export class ExamManagementComponent {
  exams = signal<any[]>([]);
  expandedExam: any = null;

  private examService = inject(ExamService);
  private loggingService = inject(LoggingService);

  ngOnInit() {
    this.fetchMyExams();
  }

  fetchMyExams() {
    this.examService.getMyExams().subscribe({
      next: (data) => {
        console.log("My Exams fetched:", data);
        this.exams.set(data.map(exam => ({ ...exam, isEditing: false, subjects: [] })));
        this.exams().forEach(exam => {
          this.examService.hasExamDistributions(exam.examId).subscribe({
            next: (hasDistributions: boolean) => {
              exam.isEditing = hasDistributions;
            },
            error: (err) => {
              // this.loggingService.onError(`Error checking distributions for exam ${exam.examId}`, err);
              console.error(`Error checking distributions for exam ${exam.examId}`, err);
              exam.isEditing = false;
            }
          });
        });
      },
      error: (err) => {
        this.loggingService.onError(err);
        console.error("Error fetching my exams", err);
      }
    });
  }

  toggleExam(exam: any) {
    if (this.expandedExam === exam) {
      this.expandedExam = null;
    } else {
      this.expandedExam = exam;

      this.examService.getSubjectByExamWithSubtopics(exam.examId).subscribe({
        next: (subject: any) => {
          // Initialize subtopics with default values
          subject.subTopics = subject.subTopics.map((sub: any) => ({
            name: sub.name,
            subtopicId: sub.subtopicId,
            percentage: 0,
            showDifficulty: false,
            basic: 0,
            intermediate: 0,
            advance: 0
          }));

          exam.subjects = [subject];

          // Fetch existing distributions if the exam is in edit mode
          if (exam.isEditing) {
  this.examService.getExamDistribution(exam.examId).subscribe({
    next: (distributions: any[]) => {
      console.log("Fetched distributions:", distributions);

      subject.subTopics.forEach((sub: any) => {
        const distribution = distributions.find(d => d.subtopicId === sub.subtopicId);
        if (distribution) {
          sub.percentage = distribution.subtopicPercentage;
          sub.basic = distribution.basicPercentage;
          sub.intermediate = distribution.intermediatePercentage;
          sub.advance = distribution.advancePercentage;
        }
      });
      
    },
    error: (err) => {
      console.error("Error fetching distribution", err);
    }
  });
}
        },
        error: (err) => {
          console.error("Error loading subjects", err);
          exam.subjects = [];
        }
      });
    }
  }

  toggleDifficulty(subtopic: any) {
    subtopic.showDifficulty = !subtopic.showDifficulty;
  }

  saveExam(exam: any, subject: any) {
  const totalSubtopicPercentage = subject.subTopics.reduce(
    (sum: number, sub: { percentage: number }) => sum + (Number(sub.percentage) || 0),
    0
  );

  if (totalSubtopicPercentage > 100) {
    this.loggingService.onWarning(`Total subtopic percentage for "${subject.subjectName}" exceeds 100%. Please adjust.`)
    return;
  }

  for (const sub of subject.subTopics) {
    const difficultyTotal = (Number(sub.basic) || 0) + (Number(sub.intermediate) || 0) + (Number(sub.advance) || 0);
    if (difficultyTotal > 100) {
      this.loggingService.onWarning(`Difficulty percentage for subtopic "${sub.name}" exceeds 100%. Please adjust.`)
      return;
    }
  }

  const requestBody = {
    examId: exam.examId,
    totalQuestions: Number(exam.totalQuestions) || 0, // 👈 Use user-entered value
    examType: exam.examType || 'Programming',
    distribution: subject.subTopics.map((sub: any) => ({
      subtopicId: sub.subtopicId,
      percentage: Number(sub.percentage) || 0,
      difficultyDistribution: {
        basic: Number(sub.basic) || 0,
        intermediate: Number(sub.intermediate) || 0,
        advance: Number(sub.advance) || 0
      }
    }))
  };

  const serviceCall = exam.isEditing
    ? this.examService.updateDistribution(requestBody)
    : this.examService.generateQuestions(requestBody);

  serviceCall.subscribe({
    next: (response) => {
      this.loggingService.onSuccess(exam.isEditing ? "Distribution Updated Successfully" : "Percentages Applied Successfully");
      exam.isEditing = true;
      this.expandedExam = null;
    },
    error: (error) => {
      this.loggingService.onError(exam.isEditing ? "Failed to update distribution." : "Failed to apply percentage.");
    }
  });
}

}
// import { Component, inject, signal } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { FormsModule } from '@angular/forms';
// import { ExamService } from '../../service/exam.service';

// @Component({
//   selector: 'app-exam-management',
//   standalone: true,
//   imports: [CommonModule, FormsModule],
//   templateUrl: './exam-management.component.html',
//   styleUrls: ['./exam-management.component.css']
// })
// export class ExamManagementComponent {
//   exams = signal<any[]>([]);
//   expandedExam: any = null;

//   private examService = inject(ExamService);

//   ngOnInit() {
//     this.fetchAllExams();
//   }

//   fetchAllExams() {
//     this.examService.getExams().subscribe({
//       next: (data) => {
//         console.log("Exams fetched:", data);
//         this.exams.set(data);
//       },
//       error: (err) => {
//         console.error("Error fetching exams", err);
//       }
//     });
//   }

//   toggleExam(exam: any) {
//   if (this.expandedExam === exam) {
//     this.expandedExam = null;
//   } else {
//     this.expandedExam = exam;

//     this.examService.getSubjectByExamWithSubtopics(exam.examId).subscribe({
//     next: (subject: any) => {
//      subject.subTopics = subject.subTopics.map((sub: any) => ({
//       name: sub.name,
//       subtopicId: sub.subtopicId,  
//       percentage: 0,
//       showDifficulty: false,
//       basic: 0,
//       intermediate: 0,
//       advance: 0
//     }));

//     exam.subjects = [subject];
//   },
//   error: (err) => {
//     console.error("Error loading subjects", err);
//     exam.subjects = [];
//   }
// });

//   }
// }

//   toggleDifficulty(subtopic: any) {
//     subtopic.showDifficulty = !subtopic.showDifficulty;
//   }

//  generateQuestions(exam: any, subject: any) {
// const totalSubtopicPercentage = subject.subTopics.reduce(
//   (sum: number, sub: { percentage: number }) => sum + sub.percentage,
//   0
// );

//   if (totalSubtopicPercentage > 100) {
//     alert(`Total subtopic percentage for "${subject.subjectName}" exceeds 100%. Please change it`);
//     return;
//   }

//   for (const sub of subject.subTopics) {
//     const difficultyTotal = (Number(sub.basic) || 0) + (Number(sub.intermediate) || 0) + (Number(sub.advance) || 0);
//     if (difficultyTotal > 100) {
//       alert(`Difficulty percentage for subtopic "${sub.name}" exceeds 100%. Please change it`);
//       return;
//     }
//   }

//   const requestBody = {
//     examId: exam.examId,
//     totalQuestions: 20,
//     distribution: subject.subTopics.map((sub: any) => ({
//       subtopicId: sub.subtopicId,
//       percentage: sub.percentage,
//       difficultyDistribution: {
//         basic: sub.basic,
//         intermediate: sub.intermediate,
//         advance: sub.advance
//       }
//     }))
//   };

//   this.examService.generateQuestions(requestBody).subscribe({
//     next: (response) => {
//       console.log("Questions generated successfully", response);
//       alert("Percentages Applied Successfully");
//     },
//     error: (error) => {
//       console.error("Error generating questions", error);
//       alert("Failed to apply percentage.");
//     }
//   });
// }

// }


// import { Component, inject, signal } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { FormsModule } from '@angular/forms';
// import { ExamService } from '../../service/exam.service';

// @Component({
//   selector: 'app-exam-management',
//   standalone: true,
//   imports: [CommonModule, FormsModule],
//   templateUrl: './exam-management.component.html',
//   styleUrls: ['./exam-management.component.css']
// })
// export class ExamManagementComponent {
//   exams = signal<any[]>([]);
//   expandedExam: any = null;

//   private examService = inject(ExamService);
//   ngOnInit() {
//     // 🔽 Static hardcoded exam-subject-subtopic structure
//     // const sampleData = [
//     //   {
//     //     examId: 1,
//     //     examName: 'Mid-Term Exam',
//     //     subjects: [
//     //       {
//     //         subjectId: 101,
//     //         subjectName: 'Mathematics',
//     //         subTopics: [
//     //           { name: 'Algebra', percentage: 0, showDifficulty: false, basic: 0, intermediate: 0, advance: 0 },
//     //           { name: 'Geometry', percentage: 0, showDifficulty: false, basic: 0, intermediate: 0, advance: 0 }
//     //         ]
//     //       },
//     //       {
//     //         subjectId: 102,
//     //         subjectName: 'Science',
//     //         subTopics: [
//     //           { name: 'Physics', percentage: 0, showDifficulty: false, basic: 0, intermediate: 0, advance: 0 },
//     //           { name: 'Biology', percentage: 0, showDifficulty: false, basic: 0, intermediate: 0, advance: 0 }
//     //         ]
//     //       }
//     //     ]
//     //   },
//     //   {
//     //     examId: 2,
//     //     examName: 'Final Exam',
//     //     subjects: [
//     //       {
//     //         subjectId: 201,
//     //         subjectName: 'English',
//     //         subTopics: [
//     //           { name: 'Grammar', percentage: 0, showDifficulty: false, basic: 0, intermediate: 0, advance: 0 },
//     //           { name: 'Literature', percentage: 0, showDifficulty: false, basic: 0, intermediate: 0, advance: 0 }
//     //         ]
//     //       }
//     //     ]
//     //   }
//     // ];

//     // this.exams.set(sampleData);
//     this.fetchAllExams();
//   }

//   fetchAllExams(){
//     this.examService.getExams().subscribe({
//       next:(data) =>{
//         console.log(data);
//         this.exams.set(data);
//       }
//     })
//   }

//  toggleExam(exam: any) {
//   if (this.expandedExam === exam) {
//     this.expandedExam = null;
//   } else {
//     this.expandedExam = exam;

//     this.examService.getSubjectByExamWithSubtopics(exam.examId).subscribe({
//       next: (subject: any) => {
//         subject.subTopics = subject.name.map((subtopicName: string) => ({
//           name: subtopicName,
//           percentage: 0,
//           showDifficulty: false,
//           basic: 0,
//           intermediate: 0,
//           advance: 0
//         }));

//         exam.subjects = [subject]; 
//       },
//       error: (err) => {
//         console.error("Error loading subjects", err);
//         exam.subjects = [];
//       }
//     });
//   }
// }

//   toggleDifficulty(subtopic: any) {
//     subtopic.showDifficulty = !subtopic.showDifficulty;
//   }

//   saveDifficulty(subtopic: any) {
//     const total = subtopic.basic + subtopic.intermediate + subtopic.advance;
//     if (total > 100) {
//       alert('Total difficulty percentage exceeds 100%. Please adjust.');
//       return;
//     }

//     alert(`Saved difficulty for ${subtopic.name}:\nBasic: ${subtopic.basic}%\nIntermediate: ${subtopic.intermediate}%\nAdvance: ${subtopic.advance}%`);
//   }
// }
