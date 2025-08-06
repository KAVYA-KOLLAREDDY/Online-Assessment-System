import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from "./navbar/navbar.component";
import { AuthService } from './service/Auth.service';
import { DashboardComponent } from "./student/dashboard/dashboard.component";
import { ToastrService } from 'ngx-toastr';
import { environment } from '../environments/environment';
import { ExamQuestionsComponent } from "./student/exam-questions/exam-questions.component";
import { ExamEnvironmentComponent } from "./student/exam-environment/exam-environment.component";
import { MyExamsComponent } from "./student/my-exams/my-exams.component";

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, NavbarComponent, ExamEnvironmentComponent, MyExamsComponent, ExamQuestionsComponent],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {;
  title = 'client'; 
}
