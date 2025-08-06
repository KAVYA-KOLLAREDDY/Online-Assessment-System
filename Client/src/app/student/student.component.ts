import { Component, OnInit } from '@angular/core';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { NavbarComponent } from "../navbar/navbar.component";
import { StudentSidebarComponent } from "./student-sidebar/student-sidebar.component";
import { SidebarComponent } from "../common/sidebar/sidebar.component";
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs';

@Component({
  selector: 'app-student',
  imports: [RouterOutlet,SidebarComponent,CommonModule],
  templateUrl: './student.component.html',
  styleUrl: './student.component.css'
})
export class StudentComponent {
  showSidebar: boolean = true;

  constructor(private router: Router) {
    this.router.events.pipe(filter(event => event instanceof NavigationEnd)).subscribe(() => {
      const currentUrl = this.router.url;
      this.showSidebar = !(
        currentUrl.startsWith('/student/exam/') || 
        currentUrl.startsWith('/student/results/') ||
        currentUrl.startsWith('/student/guidelines')
      );
    });
  }
}

