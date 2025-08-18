import { Routes } from '@angular/router';
import { AdminComponent } from './admin/admin.component';
import { CreateSubjectComponent } from './admin/create-subject/create-subject.component';
import { adminChildGuard, adminGuard } from './guards/admin.guard';
import { examinerChildGuard, examinerGuard } from './guards/examiner.guard';
import { studentChildGuard, studentGuard } from './guards/student.guard';
import { LoginComponent } from './Auth/login/login.component';
import { ExamListComponent } from './student/exam-list/exam-list.component';
import { ExamQuestionsComponent } from './student/exam-questions/exam-questions.component';
import { ResultComponent } from './student/result/result.component';
import { StudentComponent } from './student/student.component';
import { CreateExamComponent } from './trainer/create-exam/create-exam.component';
import { TrackExamsComponent } from './trainer/track-exams/track-exams.component';
import { TrainerComponent } from './trainer/trainer.component';
import { ViewSubjectsComponent } from './trainer/view-subjects/view-subjects.component';
import { ManageUsersComponent } from './admin/manage-users/manage-users.component';
import { RegisterComponent } from './Auth/register/register.component';
import { ExamUploadComponent } from './trainer/exam-upload/exam-upload.component';
import { CreateSubTopicComponent } from './trainer/create-sub-topic/create-sub-topic.component';
import { DashboardComponent } from './student/dashboard/dashboard.component';
import { ManageSubtopicsComponent } from './trainer/manage-subtopics/manage-subtopics.component';
import { examGuard } from './guards/exam.guard';
import { DummyResetComponent } from './dummy-reset/dummy-reset.component';
import { ManageExamsComponent } from './admin/manage-exams/manage-exams.component';
import { ExamManagementComponent } from './trainer/exam-management/exam-management.component';
import { ExamEnvironmentComponent } from './student/exam-environment/exam-environment.component';
import { MyExamsComponent } from './student/my-exams/my-exams.component';
import { MyCertificatesComponent } from './student/my-certificates/my-certificates.component';
import { ExamGuidelinesComponent } from './student/exam-guidelines/exam-guidelines.component';


export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: '', redirectTo: '/login', pathMatch: 'full'},
  { path: 'register', component: RegisterComponent },
  { path: 'register/:role', component: RegisterComponent },
  { path: 'dummy-reset', component: DummyResetComponent },

  {
    path: 'student',
    component: StudentComponent,
    canActivate: [studentGuard],
    canActivateChild: [studentChildGuard],
    children: [
      {
        path: '',
        redirectTo:'studentDashboard',
        pathMatch:'full',
      },
      {
        path: 'studentDashboard',
        component: DashboardComponent,
      },
      { path: 'exams', component: ExamListComponent },
      { path:'guidelines/:id', component: ExamGuidelinesComponent},
      { path: 'exam/:id', component: ExamEnvironmentComponent },
      {
        path: 'results/:id',
        component: ResultComponent,
      },
      {
        path: 'exam/:id/:subTopicId',
        component: ExamQuestionsComponent,
      },
      
      {
        path: 'examEnvironment',
        component: ExamEnvironmentComponent,
      },
      {
        path: 'myExams',
        component: MyExamsComponent,
      },
      {
        path: 'myCertificates',
        component: MyCertificatesComponent
      }
    ],
  },
  {
    path: 'admin',
    component: AdminComponent,
    canActivate: [adminGuard],
    canActivateChild: [adminChildGuard],
    children: [
      {
        path: 'createSubject',
        component: CreateSubjectComponent,
      },
      {
        path: 'manageExams',
        component: ManageExamsComponent,
      },
      {
        path: 'manageUsers',
        component: ManageUsersComponent,
      },
      {
        path: 'viewSubjects',
        component: ViewSubjectsComponent,
      },
      { path: '', redirectTo: 'manageUsers', pathMatch: 'full' }

    ]
  },
  {
    path: 'trainer',
    component: TrainerComponent,
    canActivate: [examinerGuard],
    canActivateChild: [examinerChildGuard],
    children: [
      {
        path: '',
        redirectTo:'viewSubjects',
        pathMatch:'full'
      },
      {
        path: 'viewSubjects',
        component: ViewSubjectsComponent,
      },
      {
        path: 'subTopics',
        component: CreateSubTopicComponent,
      },
      {
        path: 'createExam',
        component: CreateExamComponent,
      },
      {
        path: 'manageExams',
        component: ExamManagementComponent,
      },
      {
        path: 'trackExam',
        component: TrackExamsComponent,
      },
      {
        path: 'examUpload/:subjectId/:subjectName',
        component: ExamUploadComponent,
      },
      {
        path: 'manageSubTopics/:subjectId',
        component: ManageSubtopicsComponent,
      },
    ],
  },
];
