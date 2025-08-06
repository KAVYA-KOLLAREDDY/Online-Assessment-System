import { Component, computed, inject } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ExamService } from '../../service/exam.service';
import { AuthService } from '../../service/Auth.service';
import { LoggingService } from '../../service/logging.service';
import { ToastrService } from 'ngx-toastr';
import { handleResposne } from '../../utils/handle-response.utils';

@Component({
  selector: 'app-create-subject',
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './create-subject.component.html',
  styleUrl: './create-subject.component.css',
})
export class CreateSubjectComponent {
  subjectForm: FormGroup;
  submitted = false;
  successMessage = '';
  private authService = inject(AuthService);
  private loggingService = inject(LoggingService);
  private toastrService = inject(ToastrService);

  constructor(private fb: FormBuilder, private subjectService: ExamService) {
    const user = computed(() => this.authService.currentUser());
    console.log(user());
    this.subjectForm = this.fb.group({
      subjectName: ['', Validators.required],
      description: ['', Validators.required],
    });
  }

  onSubmit() {
    if (this.subjectForm.valid) {
      this.subjectService.createSubject(this.subjectForm.value).subscribe(
        handleResposne(this.loggingService, () => {
          this.loggingService.onSuccess('Subject Created!!');
          this.subjectForm.reset();
        })
      );
    }
  }
}
