import { NgClass, NgFor } from '@angular/common';
import { Component } from '@angular/core';
// import { MatTooltipModule } from '@angular/material/tooltip';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { matDoubleArrow } from '@ng-icons/material-icons/baseline';

@Component({
  selector: 'app-admin-sidebar',
  imports: [RouterLink],
  templateUrl: './admin-sidebar.component.html',
  styleUrl: './admin-sidebar.component.css',
  viewProviders: [provideIcons({ matDoubleArrow })],
})
export class AdminSidebarComponent {
  
}
