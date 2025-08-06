import { Component, inject, OnInit, signal } from '@angular/core';
import { ExamService } from '../../service/exam.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { LoggingService } from '../../service/logging.service';
import { handleResposne } from '../../utils/handle-response.utils';

@Component({
  selector: 'app-create-sub-topic',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './create-sub-topic.component.html',
  styleUrl: './create-sub-topic.component.css',
})
export class CreateSubTopicComponent implements OnInit {
  examService = inject(ExamService);
  private router = inject(Router);
  private toaster = inject(LoggingService);

  subTopics = signal<any[]>([]);
  subjects = signal<any[]>([]);
  selectedSubjectId: number = 0;
  subtopicNames: string[] = [''];
  message = signal('');

  ngOnInit() {
    this.examService.getAllsubjectsWithSubtopics().subscribe(
      handleResposne(this.toaster, (data) => {
        const filteredSubjects = data.filter(
          (subject: any) => !subject.alreadyHasSubtopics
        );
        this.subjects.set(filteredSubjects);
      })
    );
  }

  onSubjectChange(subjectId: number) {
    this.selectedSubjectId = subjectId;
    this.subTopics.set([]);

    if (subjectId > 0) {
      this.examService.getAllSubtopicsBySubject(subjectId).subscribe(
        handleResposne(this.toaster, (data: any) => {
          const subtopicList = data.name.map((subtopicName: string) => ({
            name: subtopicName,
            subjectId: data.subjectId,
            subjectName: data.subjectName,
          }));

          this.subTopics.set(subtopicList);
        })
      );
    }
  }
  addSubtopicField() {
    this.subtopicNames.push('');
  }

  onSubmit() {
  const validNames = this.subtopicNames
    .map((s) => s.trim())
    .filter((s) => s.length > 0);

  if (!this.selectedSubjectId || validNames.length === 0) {
    this.message.set(
      'Please select a subject and enter at least one subtopic.'
    );
    return;
  }

  const payload = {
    subjectId: this.selectedSubjectId,
    subjectName:
      this.subjects().find(
        (subject) => subject.subjectId === this.selectedSubjectId
      )?.subjectName || '',
    alreadyHasSubtopics: false,
    subTopics: validNames.map(name => ({ name }))
  };

  console.log('Creating subtopics with payload:', payload);

  this.examService.createSubTopics(payload).subscribe(
    handleResposne(this.toaster, () => {
      this.subtopicNames = [''];
      this.selectedSubjectId = 0;
      this.subTopics.set([]);

      const updatedSubjects = this.subjects().filter(
        (subject) => subject.subjectId !== payload.subjectId
      );
      this.subjects.set([...updatedSubjects]);
      this.toaster.onSuccess('Subtopics Created!!');
    })
  );
}

  trackByIndex(index: number, item: any): any {
    return index;
  }
}

// export class CreateSubTopicComponent {
//   examService = inject(ExamService);
//   private router = inject(Router);

//   subTopics = signal<any[]>([]);
//   subjects = signal<any[]>([]);
//   selectedSubjectId: number = 0;
//   subtopicName: string = '';
//   message = signal('');

//   ngOnInit() {
//     this.examService.getSubjects().subscribe({
//       next: (data) => {
//         console.log('Subjects loaded:', data);
//         this.subjects.set(data);
//       },
//       error: (err) => console.error('Error fetching subjects:', err)
//     });
//   }

//   onSubjectChange(subjectId: number) {
//     this.selectedSubjectId = subjectId;
//     this.subTopics.set([]);

//     if (subjectId > 0) {
//       this.examService.getAllSubtopicsBySubject(subjectId).subscribe({
//         next: (data) => {
//           console.log(`Subtopics for subject ${subjectId}:`, data);
//           this.subTopics.set(data);
//         },
//         error: (err) => console.error('Error loading subject subtopics:', err)
//       });
//     }
//   }
//   onSubmit() {
//     if (!this.selectedSubjectId || !this.subtopicName.trim()) {
//       this.message.set('Please select a subject and enter subtopic name.');
//       return;
//     }
//     const subtopicPayload = {
//       subjectId: this.selectedSubjectId,
//       name: this.subtopicName.trim(),
//     };
//     console.log('Creating subtopic with payload:', subtopicPayload);

//     this.examService.createSubTopics(subtopicPayload).subscribe({
//       next: () => {
//         console.log('Subtopic creation success');
//         this.subtopicName = '';

//         this.examService.getAllSubtopicsBySubject(this.selectedSubjectId).subscribe({
//           next: (data) => {
//             console.log('Updated subtopics after creation:', data);
//             this.subTopics.set(data);
//           },
//           error: (err) => console.error('Error fetching subtopics:', err)
//         });
//       },
//       error: (err) => {
//         console.error('Error creating subtopic:', err);
//         this.message.set('❌ Failed to create subtopic.');
//       },
//     });
//   }
// }
// onSubmit() {
//   const validNames = this.subtopicNames.map(s => s.trim()).filter(s => s.length > 0);

//   if (!this.selectedSubjectId || validNames.length === 0) {
//     this.message.set('Please select a subject and enter at least one subtopic.');
//     return;
//   }

//   const payload = {
//     subjectId: this.selectedSubjectId,
//     name: validNames,
//     subjectName: this.subjects().find(subject => subject.subjectId === this.selectedSubjectId)?.subjectName || ''
//   };

//   console.log('Creating subtopics with payload:', payload);

//   this.examService.createSubTopics(payload).subscribe({
//     next: () => {
//       // ✅ Reset fields
//       this.subtopicNames = [''];

//       console.log('Subjects before:', this.subjects());

//       // ✅ Correctly update the signal by creating a new array
//       const updatedList = this.subjects().filter(subject => subject.subjectId !== payload.subjectId);
//       this.subjects.set([...updatedList]);

//       console.log('Subjects after:', updatedList);
//       this.selectedSubjectId = 0;
//       this.message.set('✅ Subtopics created and subject removed!');
//     },
//     error: (err) => {
//       console.error('Error creating subtopics:', err);
//       this.message.set('❌ Failed to create subtopics.');
//     }
//   });
// }
