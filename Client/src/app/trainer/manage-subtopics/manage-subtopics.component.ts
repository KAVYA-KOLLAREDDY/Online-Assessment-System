import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, FormGroup, FormsModule, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { ExamService } from '../../service/exam.service';
import { CommonModule } from '@angular/common';
import { LoggingService } from '../../service/logging.service';
import * as XLSX from 'xlsx';
import FileSaver from 'file-saver';


@Component({
  selector: 'app-manage-subtopics',
  imports: [ReactiveFormsModule, CommonModule, FormsModule],
  templateUrl: './manage-subtopics.component.html',
  styleUrl: './manage-subtopics.component.css'
})

export class ManageSubtopicsComponent implements OnInit {
  loader: boolean = false;
  subject = signal<any>(null);
  subtopics = signal<any[]>([]);

  selectedSubjectId: number = 0;
  selectedSubjectName: string = '';
  subtopicForm: FormGroup;

  showSubtopicForm: boolean = false;
  private toaster = inject(LoggingService);

  private activatedRoute = inject(ActivatedRoute);
  private examService = inject(ExamService);
  private fb = inject(FormBuilder);

  constructor() {
    this.subtopicForm = this.fb.group({
      subTopicName: [''],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.activatedRoute.paramMap.subscribe(params => {
      this.loader = true;
      const subjectId = Number(params.get('subjectId'));

      if (subjectId) {
        this.examService.getSubject(subjectId).subscribe({
          next: (data) => {
            console.log('Subject Loaded:',data);
            this.subject.set(data);
            this.loadSubtopics(subjectId);
            console.log(this.loadSubtopics);
            this.loader = false;
          },
          error: (err) => {
            this.loader = false;
            console.error('Error loading subject:', err);  
          }
        })
      } else {
        alert("Invalid subject!")
        this.loader = false;
      }
    });
  }

  onSubjectChange(subjectId: number) {
    this.selectedSubjectId = subjectId;
    this.loadSubtopics(subjectId);
    this.showSubtopicForm = false;
  }

  loadSubtopics(subjectId: number) {
    this.examService.getAllSubtopicsBySubject(subjectId).subscribe({
      next: (data) => {
        this.subtopics.set(data);
        console.log('Loaded Data:',data);

        const allSubtopics = data.flatMap((subject: any) => subject.subTopics || []);
      
      this.subtopics.set(allSubtopics); 
      },
      error: (err) => console.error('Error fetching subtopics:', err)
    });
  }

  toggleSubtopicForm(choice: string) {
    this.showSubtopicForm = (choice === 'yes');
    if (!this.showSubtopicForm) {
      this.subtopicForm.reset();
    }
  }

  onSubmit() {
    console.log("Subject value on submit:", this.subject());

  if (!this.subject() || !this.subject().subjectId) {
    alert("Subject not loaded correctly. Please wait or refresh the page.");
    return;
  }

  const payload = {
    subjectId: this.subject().subjectId,
    subjectName: this.subject().subjectName,
    alreadyHasSubtopics: true,
    subTopics: [
      {
        name: this.subtopicForm.value.subTopicName
      }
    ]
  };

  console.log('Payload:', payload);

  this.examService.createSubTopics(payload).subscribe({
    next: () => {
      this.toaster.onSuccess("Subtopic created successfully !!");
      this.subtopicForm.reset();
      this.showSubtopicForm = false;
      this.loadSubtopics(this.subject().subjectId);
    },
    error: (err) => console.error('Failed to create subtopic:', err)
  });
}

//   exportToExcel(): void {
//   const dataToExport = this.subtopics().map(sub => ({
//     'Subtopic ID': sub.subtopicId,
//     'Subtopic Name': sub.name
//   }));

//   const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(dataToExport);
//   const workbook: XLSX.WorkBook = {
//     Sheets: { 'Sheet 2': worksheet },
//     SheetNames: ['Sheet 2']
//   };

//   const excelBuffer: any = XLSX.write(workbook, {
//     bookType: 'xlsx',
//     type: 'array'
//   });

//   const blobData: Blob = new Blob([excelBuffer], {
//     type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
//   });

//   FileSaver.saveAs(blobData, 'QuestionsForSubtopics.xlsx');
// }
exportToExcel(): void {
  // ✅ Sheet 1: Sample Questions
  const sheet1Data = [
    [
      'Sub Topic Id', 'Question', 'Sub Topic', 'Q_type', 'Difficulty Level',
      'Option A', 'Option B', 'Option C', 'Option D', 'Correct Options', 'Marks'
    ],
    [11, 'Which keyword is used to create a constant in Java?', 'Java Basics', 'MCQ', 'Basic', 'const', 'final', 'constant', 'static', 'B', 1],
    [12, 'Which of the following statements is correct regarding the break statement in loops?', 'Control Flow & Loops', 'MSQ', 'Intermediate', 'It terminates the loop immediately', 'It skips the remaining code in the loop', 'It skips the current iteration', 'It is used to terminate switch statements', 'A, D', 1],
    [12, "What is the output of the following code: 'for (int i = 0; i < 5; i++) { if (i == 3) break; System.out.println(i); }'?", 'Control Flow & Loops', 'CodingSnippet', 'Advance', '0 1 2', '0 1 2 3', '0 1 2 3 4', 'Error', 'A', 1]
  ];
  const sheet1 = XLSX.utils.aoa_to_sheet(sheet1Data);

  // ✅ Sheet 2: Instructions + Subtopics
  const sheet2Data = [
    ['📘 INSTRUCTIONS FOR QUESTIONS PREPARATION'],
    [],
    ['Please carefully read the instructions below before preparing your question paper.'],
    [],
    [' 1. Subtopics Mapping'],
    ['The table below contains the list of valid Sub Topics along with their corresponding Sub Topic IDs'],
    ['In the “Questions” sheet (Sheet1), use the exact same Sub Topic names from this table.'],
    ['To automatically populate the “Sub Topic Id” column based on the Sub Topic name, use the following formula in cell A2 of Sheet1 and drag it down to apply for all rows: =VLOOKUP(C2,Sheet2!A:B,2,FALSE)'],
    [],
    ['2. Question Type (Q_type) Format'],
    [' Only the following values are allowed (case-sensitive and no spaces): MCQ, MSQ, CodingSnippet'],
    [],
    [' 3. Difficulty Level Format:'],
    ['This column must contain one of the following values only (case-sensitive): Basic Intermediate Advance'],
    [],
    [' 4. Option Columns and Correct Answers'],
    ['Use the columns Option A, Option B, Option C, and Option D to list all possible answers.'],
    ['In the Correct Options column, specify the correct answer(s) by using the corresponding option letter(s).'],
    ['For MCQ, enter one letter (e.g., B) For MSQ, enter multiple letters separated by commas (e.g., A, C)'],
    [],
    [' 5. Marks'],
    ['Specify the marks allotted for each question in the Marks column (e.g., 1, 2, etc.).'],
    [],
    ['⚠️ 🔍 Important Notes:'],
    ['❗ Ensure you follow these instructions exactly. Incorrect formatting or missing values will result in errors while uploading the Excel file.'],
    ['✅ We have included 3 sample rows in the “Questions” sheet (Sheet1) to demonstrate the expected format. Please delete these rows before uploading your own questions.'],
  
  ];
const sheet2 = XLSX.utils.aoa_to_sheet(sheet2Data);

// Add an empty row before subtopics (optional for spacing)
sheet2Data.push([]);
sheet2Data.push(['Subtopic ID', 'Subtopic Name']); // Header if needed

const dataToExport = this.subtopics().map(sub => ({
  'Subtopic ID': sub.subtopicId,
  'Subtopic Name': sub.name
}));

// 👇 Append subtopics table starting at row (e.g., row 30)
XLSX.utils.sheet_add_json(sheet2, dataToExport, {
  origin: -1, // Appends after existing data
  skipHeader: true // We already added header manually above
});

  // 🧾 Final Workbook
  const worksheet: XLSX.WorkSheet = XLSX.utils.json_to_sheet(dataToExport);
  const workbook: XLSX.WorkBook = {
    Sheets: {
      
      'Questions': sheet1,
      'Instructions': sheet2
    },
    SheetNames: ['Questions', 'Instructions']
  };

  // 💾 Save
  const excelBuffer: any = XLSX.write(workbook, {
    bookType: 'xlsx',
    type: 'array'
  });

  const blob: Blob = new Blob([excelBuffer], {
    type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  });

  FileSaver.saveAs(blob, 'Question_Template_With_Instructions.xlsx');
 }
}
