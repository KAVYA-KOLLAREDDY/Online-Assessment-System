import { Component, computed, ElementRef, inject, ViewChild } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { MenuService } from '../../service/Menu.service';
import { AuthService } from '../../service/Auth.service';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule,RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css',
})
export class SidebarComponent {
  private authService = inject(AuthService);
  menus = computed(() => this.authService.currentSidebar());

  showSidebar: boolean = true;

  constructor(private router: Router) {
    this.router.events.subscribe(() => {
      const currentUrl = this.router.url;
      this.showSidebar = !currentUrl.startsWith('/student/exam/') && !currentUrl.startsWith('/student/results/');
      console.log('Sidebar visible?', this.showSidebar, 'Current URL:', currentUrl);
    });
  }

  ngOnInit() {
    this.authService.fetchSidebar().subscribe({
      next: () => {
        console.log("🔍 Sidebar Loaded:", this.menus());
      }
    });
  }

}
