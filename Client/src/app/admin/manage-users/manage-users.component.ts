import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule, NgFor } from '@angular/common';
import { Users } from '../../models/student.model';
import { ExamService } from '../../service/exam.service';
import { StudentResponse } from '../../models/studentResponse.model';
import { Router } from '@angular/router';
import { handleResposne } from '../../utils/handle-response.utils';
import { LoggingService } from '../../service/logging.service';

@Component({
  selector: 'app-manage-users',
  standalone: true,
  imports: [CommonModule, NgFor],
  templateUrl: './manage-users.component.html',
  styleUrl: './manage-users.component.css',
})
export class ManageUsersComponent {
  private userService = inject(ExamService);
  users = signal<any[]>([]);
  private router = inject(Router);
  private loggingService = inject(LoggingService);

  ngOnInit() {
    this.fetchAllStudents();
  }

  private fetchAllStudents() {
    this.userService.getAllUsers().subscribe(
      handleResposne(this.loggingService, (data) => {
        console.log("All students data:",data);
        this.users.set(data);
      })
    );
  }

  editUser(userId: number, status: boolean) {
  console.log("Sending to backend:", { userId, isActive: status, isLocked: false });

  this.userService.changeActiveStatus(userId, status).subscribe(
    handleResposne(this.loggingService, () => {
      this.loggingService.onSuccess('Status Updated Successfully');
      this.fetchAllStudents();
    })
  );
}

}
