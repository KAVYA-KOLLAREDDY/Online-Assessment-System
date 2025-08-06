import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { jwtDecode } from 'jwt-decode';
import { tap } from 'rxjs';
import { TokenService } from './Token.service';
import { CommonService } from './Common.service';
import { environment } from '../../environments/environment';
import { Router } from '@angular/router';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private envUrl = environment.apiUrl;
  private API = `${this.envUrl}/auth`;
  private http = inject(HttpClient);
  private tokenService = inject(TokenService);
  private commonService = inject(CommonService);
  private sidebar = signal<any>(null);
  private router = inject(Router);

  private user = signal<any>(
    localStorage.getItem('access_token')
      ? jwtDecode(localStorage.getItem('access_token')!)
      : null
  );
  currentUser = this.user.asReadonly();
  currentSidebar = this.sidebar.asReadonly();

  private tokenResponse(data: any) {
    this.tokenService.setAccessToken(data.accessToken);
    const jwtDetails: any = jwtDecode(data.accessToken);
    this.user.set(jwtDetails);
  }

  login(credentials: { email: string; password: string }) {
    return this.commonService
      .post(`${this.API}/login`, credentials, {
        withCredentials: true,
      })
      .pipe(
        tap({
          next: (data) => {
            this.tokenResponse(data);
          },
        })
      );
  }

  logout() {
    return this.commonService
      .post(`${this.API}/logout`, null, {
        withCredentials: true,
      })
      .pipe(
        tap({
          next: () => {
            this.tokenService.clear();
            this.user.set(null);
            this.router.navigate(['/login']);
          },
          error: () => {
            this.tokenService.clear();
            this.user.set(null);
            this.router.navigate(['/login']);
          },
        })
      );
  }

  refreshAccessToken() {
    return this.commonService
      .post(`${this.API}/refresh-access-token`, null, {
        withCredentials: true,
      })
      .pipe(
        tap({
          next: (data) => {
            this.tokenResponse(data);
          },
        })
      );
  }

  getSample() {
    return this.http.get(`${this.envUrl}/sample`, {
      responseType: 'text',
    });
  }

  fetchSidebar() {
    return this.commonService.get(`${this.envUrl}/menu`).pipe(
      tap({
        next: (data) => {
          this.sidebar.set(data);
        },
      })
    );
  }

  getPermissonViaRoute(url: string) {
    return computed(() => {
      const sidebar = this.currentSidebar();
      if (!sidebar || !sidebar.menu) return null;

      for (let menu of sidebar.menu) {
        const menuUrl = menu.url?.trim() ?? '';
        const trimmedUrl = url.trim();

        if (menuUrl === '') {
          for (let subMenu of menu.subMenu) {
            if (subMenu.url?.trim() === trimmedUrl) {
              return subMenu.accessType;
            }
          }
        } else {
          if (menuUrl === trimmedUrl) {
            return menu.subMenu[0]?.accessType ?? null;
          }
        }
      }
      return null;
    });
  }
}
