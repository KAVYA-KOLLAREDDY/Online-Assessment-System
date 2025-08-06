import { Component, inject } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ExamService } from '../../service/exam.service';
import { ActivatedRoute } from '@angular/router';
import { handleResposne } from '../../utils/handle-response.utils';
import { LoggingService } from '../../service/logging.service';

@Component({
  selector: 'app-create-exam',
  imports: [ReactiveFormsModule],
  templateUrl: './create-exam.component.html',
  styleUrl: './create-exam.component.css',
})
export class CreateExamComponent {
  examForm: FormGroup;
  subjectName: string = '';

  private examService = inject(ExamService);
  private route = inject(ActivatedRoute);
  private fb = inject(FormBuilder);
  private loggingService = inject(LoggingService);

  constructor() {
    this.examForm = this.fb.group({
      title: ['', Validators.required],
      description: ['', Validators.required],
      date: ['', Validators.required],
    });

    this.route.queryParams.subscribe((params) => {
      this.subjectName = params['subjectName'] || 'need to selected!!';
    });
  }
  onSubmit() {
    if (this.examForm.valid) {
      const examData = this.examForm.value;

      console.log('Exam Data Submitted:', examData);

      this.examService.createExam(examData).subscribe(
        handleResposne(this.loggingService, (data) => {
          this.loggingService.onSuccess('Exam Created Successfully!');
          this.examForm.reset();
        })
      );
    } else {
      this.loggingService.onWarning('Please fill out all fields correctly.');
    }
  }
}
