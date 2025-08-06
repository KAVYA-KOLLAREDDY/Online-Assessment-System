import { NgClass, NgFor } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { NgIcon, provideIcons } from '@ng-icons/core';
import { matDoubleArrow } from '@ng-icons/material-icons/baseline';
import { MenuService } from '../../service/Menu.service';

@Component({
  selector: 'app-trainer-sidebar',
  imports: [RouterLink],
  templateUrl: './trainer-sidebar.component.html',
  styleUrl: './trainer-sidebar.component.css',
  viewProviders: [provideIcons({ matDoubleArrow })],
})
export class TrainerSidebarComponent {
  private menuService = inject(MenuService);
  menus = signal<any>(null);

  ngOnInit() {
    this.menuService.getMenu().subscribe({
      next: (data) => {
        console.log(data);
        this.menus.set(data);
      },
    });
  }
}
