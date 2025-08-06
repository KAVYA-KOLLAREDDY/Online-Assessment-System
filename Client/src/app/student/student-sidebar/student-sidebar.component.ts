import { NgClass, NgFor } from '@angular/common';
import { Component, inject } from '@angular/core';
// import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { matDoubleArrow } from '@ng-icons/material-icons/baseline';
import { MenuService } from '../../service/Menu.service';

@Component({
  selector: 'app-student-sidebar',
  imports: [RouterLink],
  templateUrl: './student-sidebar.component.html',
  styleUrl: './student-sidebar.component.css',
  viewProviders: [provideIcons({ matDoubleArrow })],
})
export class StudentSidebarComponent {
  examId: number = 76;
  private menuService = inject(MenuService);


  ngOnInit() {
    this.menuService.getMenu().subscribe({
      next: (data) => {
        console.log(data);
      },
    });
  }
}
