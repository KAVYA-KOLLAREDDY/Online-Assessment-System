import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from "../navbar/navbar.component";
import { AdminSidebarComponent } from "./admin-sidebar/admin-sidebar.component";
import { SidebarComponent } from "../common/sidebar/sidebar.component";

@Component({
  selector: 'app-admin',
  imports: [RouterOutlet, NavbarComponent, AdminSidebarComponent, SidebarComponent],
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.css'
})

export class AdminComponent {

}
