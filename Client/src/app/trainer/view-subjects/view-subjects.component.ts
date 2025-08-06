import {
  Component,
  computed,
  effect,
  inject,
  OnInit,
  signal,
  Signal,
} from '@angular/core';
import { ExamService } from '../../service/exam.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { CommonModule, NgClass } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../service/Auth.service';

@Component({
  selector: 'app-view-subjects',
  imports: [ReactiveFormsModule, CommonModule, NgClass, FormsModule],
  templateUrl: './view-subjects.component.html',
  styleUrl: './view-subjects.component.css',
})
export class ViewSubjectsComponent implements OnInit {
  subjects = signal<any[]>([]);
  
  selectedSubject: any = null;
  selectedStatus: string = 'ACTIVE'; 

  private router = inject(Router);
  private authService = inject(AuthService);
  currentPermission = signal<any>(null);

  statuses = ['ACTIVE', 'INACTIVE'];

  showModal = false;

  openModal() {
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
  }

  constructor(private subjectService: ExamService) {
    effect(() => {
      const permission = this.permissionSignal();
      console.log(permission);
      this.currentPermission.set(permission);
    });
  }

  permissionSignal = this.authService.getPermissonViaRoute(
    this.router.url.split('/')[this.router.url.split('/').length - 1]
  );

  ngOnInit() {
    this.fetchSubjects();
  }

  fetchSubjects() {
    this.subjectService.getSubjects().subscribe({
      next: (data) => {
        console.log(data);
        this.subjects.set(data);
      },
      error: (error) => {
        console.error('Error:', error);
      },
    });
  }

  createExam(subject: any) {
    console.log(subject);
    const subjectId = subject.subjectId;
    const encodedName = encodeURIComponent(subject.subjectName);
    console.log('encodedname:', encodedName);
    this.router.navigate(['/trainer/examUpload', subjectId, encodedName]);
  }

  manageSubtopics(subject: any) {
    const subjectId = subject.subjectId;
    this.router.navigate(['/trainer/manageSubTopics', subjectId]);
  }

openModalWithSubject(subject: any) {
  this.selectedSubject = subject;
  this.selectedStatus = subject.status;
  this.showModal = true;
}

updateSubjectStatus() {
  if (!this.selectedSubject) return;

  const updatedStatus = this.selectedStatus;
  const subjectId = this.selectedSubject.subjectId;

  this.subjectService
    .updateSubjectStatus(subjectId, updatedStatus)
    .subscribe({
      next: () => {
        this.fetchSubjects();
        this.closeModal();
      },
      error: (err) => {
        console.error('Update failed', err);
      },
    });
}

}
