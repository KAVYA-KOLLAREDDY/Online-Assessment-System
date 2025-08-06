import { CommonModule } from '@angular/common';
import { Component, computed, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { AuthService } from '../../service/Auth.service';
import { ExamService } from '../../service/exam.service';
import { LoggingService } from '../../service/logging.service';
import { handleResposne } from '../../utils/handle-response.utils';
import { bootstrapAt,bootstrapKeyFill} from '@ng-icons/bootstrap-icons';
import { NgIcon, provideIcons } from '@ng-icons/core';


@Component({
  selector: 'app-login',
  imports: [FormsModule, CommonModule, RouterLink, NgIcon],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
  providers:[provideIcons({bootstrapAt,bootstrapKeyFill})]

})
export class LoginComponent {
  email = '';
  password = '';
  examService = inject(ExamService);
  private authService = inject(AuthService);
  user = computed(() => this.authService.currentUser());
  router = inject(Router);
  errorMessage: string = '';
  loginForm: any;

  private loggingService = inject(LoggingService);

  submitted = false;

  onSubmit(form: NgForm) {
    console.log(form);
    if (form.valid) {
      this.submitted = true;
      this.authService.login(form.value).subscribe(
        handleResposne(this.loggingService, (data) => {
          // this.loggingService.onSuccess('Logged in successfully!');
          const role = this.user().authorities;
          if (role === 'ROLE_EXAMINER') {
            this.router.navigate(['/trainer', 'viewSubjects']);
          } else if (role === 'ROLE_STUDENT') {
            this.router.navigate(['/student', 'exams']);
          } else if (role === 'ROLE_ADMIN') {
            this.router.navigate(['/admin', 'manageExams']);
          }
        }, () => {
          this.submitted = false;
        })
      );
    }
  }
}
