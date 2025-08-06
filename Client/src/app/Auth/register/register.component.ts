import { Component, inject } from '@angular/core';
import { AuthService } from '../../service/Auth.service';
import { ActivatedRoute, Router, RouterLink, RouterOutlet } from '@angular/router';
import { FormsModule, NgForm } from '@angular/forms';
import { ExamService } from '../../service/exam.service';
import { LoggingService } from '../../service/logging.service';
import { CommonModule } from '@angular/common';
import { handleResposne } from '../../utils/handle-response.utils';

@Component({
  selector: 'app-register',
  imports: [CommonModule, FormsModule,RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  role: string = '';
  name: string = '';
  email: string = '';
  password: string = '';
  private loggingService = inject(LoggingService);

  constructor(private route: ActivatedRoute, private authService: ExamService) {
    // Get the dynamic role from the URL
    this.role = this.route.snapshot.paramMap.get('role') || '';
  }

  registerUser() {
    const formData = {
      name: this.name,
      email: this.email,
      password: this.password
    };
  
    const registrationData = { ...formData, role: this.role };

    this.authService.register(registrationData, this.role).subscribe(handleResposne(this.loggingService, (response) => {
      this.loggingService.onSuccess("Registration Successful!");
    }))
  }
  
}