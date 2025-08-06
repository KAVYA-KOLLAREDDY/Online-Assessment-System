import { Component, inject, input } from '@angular/core';
import {
  FormBuilder,
  FormGroup,
  FormsModule,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { ExamService } from '../../service/exam.service';
import { ActivatedRoute, ActivationStart } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import * as XLSX from 'xlsx';
import { Observable } from 'rxjs';
import { CommonModule } from '@angular/common';
import { LoggingService } from '../../service/logging.service';
import { handleResposne } from '../../utils/handle-response.utils';
import { AuthService } from '../../service/Auth.service';

@Component({
  selector: 'app-exam-upload',
  imports: [ReactiveFormsModule, CommonModule, FormsModule],
  templateUrl: './exam-upload.component.html',
  styleUrl: './exam-upload.component.css',
})
export class ExamUploadComponent {
  private examService = inject(ExamService);
  private route = inject(ActivatedRoute);
  private toaster = inject(LoggingService);
  private authService = inject(AuthService);

  private loggingService = inject(LoggingService);
  formSubmit = false;

  currentUser: any = null;

  minDate: string = '';
  subjectName: string = '';
  examData = {
    title: '',
    description: '',
    subjectId: '',
    activeFrom: '',
    activeTo: '',
    duration: '',
  };

  excelFile: File | null = null;
  fileError: string = '';

  onFileChange(event: any): void {
    const file = event.target.files[0];
    const validTypes = [
      'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
      'application/vnd.ms-excel',
      'text/csv',
    ];

    if (
      file &&
      (validTypes.includes(file.type) || file.name.match(/\.(xls|xlsx|csv)$/))
    ) {
      this.excelFile = file;
      this.fileError = '';
    } else {
      this.excelFile = null;
      this.fileError =
        'Please upload a valid Excel file (.xls, .xlsx, or .csv)';
      event.target.value = '';
    }
  }

  onSubmit(form: any): void {
    if (form.invalid || !this.excelFile) {
      this.fileError = !this.excelFile ? 'Excel file is required.' : '';
      alert(
        '❌ Please fill all required fields and upload a valid Excel file.'
      );
      return;
    }

    if (this.examData.activeTo < this.examData.activeFrom) {
      alert('❌ "Active To" date must be after "Active From" date.');
      return;
    }

    this.formSubmit = true;

    const formData = new FormData();
    formData.append('title', this.examData.title);
    formData.append('description', this.examData.description);
    formData.append('file', this.excelFile);
    formData.append('startTime', this.examData.activeFrom);
    formData.append('endTime', this.examData.activeTo);
    formData.append('duration', this.examData.duration);
    formData.append('subjectId', this.examData.subjectId);
    formData.append('subjectName', this.subjectName);


    this.examService.uploadLatest(formData).subscribe(
      handleResposne(this.loggingService, () => {
        this.toaster.onSuccess('Exam Uploaded Successfully!!');
        
        form.resetForm();
        this.examData = {
          title: '',
          description: '',
          subjectId: this.examData.subjectId,
          activeFrom: '',
          activeTo: '',
          duration: '',
        };
        console.log('uploaded Exam',this.examData);
        this.excelFile = null;
        this.fileError = '';
      }, () => {
        this.formSubmit = false;
      })
    );
  }
     
  setMinDate(): void {
    const today = new Date();
    const yyyy = today.getFullYear();
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const dd = String(today.getDate()).padStart(2, '0');
    this.minDate = `${yyyy}-${mm}-${dd}`;
  }

  ngOnInit(): void {
    this.subjectName = decodeURIComponent(
      this.route.snapshot.paramMap.get('subjectName') || ''
    );
    const subjectIdParam = this.route.snapshot.paramMap.get('subjectId');
    if (subjectIdParam) {
      this.examData.subjectId = subjectIdParam;
    }
    this.setMinDate();

    this.currentUser = this.authService.currentUser();
    console.log('Logged in user:', this.currentUser);

  }
}

// export class ExamUploadComponent {
//     // subjectName = input.required();

//     private examService = inject(ExamService);
//     private route = inject(ActivatedRoute);

//     subjectName: string = '';
//     examData = {
//       title: '',
//       description: '',
//       subjectId: '',
//       activeFrom:'',
//       activeTo:'',
//       duration:''
//     };

//     excelFile: File | null = null;

//     onFileChange(event: any) {
//       const file = event.target.files[0];
//       if (file && (file.type === 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
//         || file.type === 'application/vnd.ms-excel'
//         || file.type === 'text/csv' || file.name.endsWith('.csv'))) {
//         this.excelFile = file;
//       } else {
//         alert("Please upload a valid Excel file (.xls or .xlsx)");
//         event.target.value = '';
//       }
//     }

//     onSubmit(form: any) {
//       if (form.invalid || !this.excelFile) {
//         alert("Please fill all fields and upload the Excel file.");
//         return;
//       }

//       const formData = new FormData();
//       formData.append('title', this.examData.title);
//       formData.append('description', this.examData.description);
//       formData.append('file', this.excelFile);
//       formData.append('startTime', this.examData.activeFrom);
//       formData.append('endTime', this.examData.activeTo);
//       formData.append('duration', this.examData.duration);
//       formData.append('subjectId', this.examData.subjectId);
//       formData.append('subjectName', this.subjectName);

//       console.log("Uploading with:", {
//         title: this.examData.title,
//         description: this.examData.description,
//         fileName: this.excelFile.name,
//         subjectName: this.subjectName,
//         subjectId: this.examData.subjectId
//       });

//       this.examService.uploadLatest(formData).subscribe({
//         next: (data) => {
//           alert("✅ Exam uploaded successfully!");
//           form.resetForm();
//           this.examData = {
//             title: '',
//             description: '',
//             activeFrom:'',
//             activeTo:'',
//             duration:'',
//             subjectId: this.examData.subjectId,
//           };
//           this.excelFile = null;
//         },
//         error: (err) => {
//           alert("❌ Upload failed. Please try again.");
//           console.error(err);
//         }
//       });
//     }

//     ngOnInit(): void {
//       this.subjectName = decodeURIComponent(this.route.snapshot.paramMap.get('subjectName') || '');
//       const subjectIdParam = this.route.snapshot.paramMap.get('subjectId');

//       if (subjectIdParam) {
//         this.examData.subjectId = subjectIdParam;
//       }
//       console.log('Subject received:', this.subjectName);
//       console.log('Subject ID:', this.examData.subjectId);
//     }

//   }
