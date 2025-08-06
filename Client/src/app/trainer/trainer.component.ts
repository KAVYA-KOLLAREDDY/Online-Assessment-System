import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { NavbarComponent } from "../navbar/navbar.component";
import { TrainerSidebarComponent } from "./trainer-sidebar/trainer-sidebar.component";
import { SidebarComponent } from "../common/sidebar/sidebar.component";

@Component({
  selector: 'app-trainer',
  imports: [RouterOutlet, NavbarComponent, TrainerSidebarComponent, SidebarComponent],
  templateUrl: './trainer.component.html',
  styleUrl: './trainer.component.css'
})
export class TrainerComponent {

}
