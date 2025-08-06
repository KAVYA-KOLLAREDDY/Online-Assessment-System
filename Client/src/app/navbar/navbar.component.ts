import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AuthService } from '../service/Auth.service';
import { CommonModule } from '@angular/common';
import { handleResposne } from '../utils/handle-response.utils';
import { LoggingService } from '../service/logging.service';

@Component({
  selector: 'app-navbar',
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.css',
})
export class NavbarComponent {
  private authService = inject(AuthService);
  user = computed(() => this.authService.currentUser());
  showNavbar: boolean = true;
  private loggingService = inject(LoggingService);

  constructor(private router: Router) {
    this.router.events.subscribe(() => {
      const currentUrl = this.router.url;
      this.showNavbar =
          !currentUrl.startsWith('/student/exam/')&&
          !['/login', '/register'].includes(currentUrl)&&
          !currentUrl.startsWith('/register')&&
          !currentUrl.startsWith('/student/guidelines/');
      console.log('Navbar visible?', this.showNavbar, 'Current URL:', currentUrl);
    });
  }

  onLogout() {
    this.authService.logout().subscribe(
      handleResposne(this.loggingService, (data) => {
        // this.loggingService.onInfo('Logged Out Successfully!');
        this.router.navigate(['/login']);
      })
    );
  }
}
