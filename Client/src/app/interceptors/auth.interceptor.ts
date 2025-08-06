import { BehaviorSubject, catchError, of, switchMap, throwError } from 'rxjs';
import { AuthService } from '../service/Auth.service';
import { TokenService } from '../service/Token.service';
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

let isRefreshing = false;
let refreshTokenSubject = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const authService = inject(AuthService);
  const tokenService = inject(TokenService);

  const skipAuth = ['/login', '/register', '/refresh', '/logout'];
  const urlPath = new URL(req.url, window.location.origin).pathname;

  if (skipAuth.some((endpoint) => urlPath.includes(endpoint))) {
    return next(req);
  }

  const accessToken = tokenService.getAccessToken();
  const isExpired = tokenService.isAccessTokenExpired();

  if (!isExpired && accessToken) {
    return next(
      req.clone({
        setHeaders: { Authorization: `Bearer ${accessToken}` },
      })
    );
  }

  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    return authService.refreshAccessToken().pipe(
      switchMap(() => {
        isRefreshing = false;
        const newAccessToken = tokenService.getAccessToken();
        refreshTokenSubject.next(newAccessToken);

        return next(
          req.clone({
            setHeaders: { Authorization: `Bearer ${newAccessToken}` },
          })
        );
      }),
      catchError((err) => {
        isRefreshing = false;

        if (err.status == 401) {
          authService.logout().subscribe();
        }

        authService.logout().subscribe();
        return throwError(() => err);
      })
    );
  }

  // Wait until refreshTokenSubject gets a value (token is refreshed)
  return refreshTokenSubject.pipe(
    switchMap((token) => {
      if (token) {
        return next(
          req.clone({
            setHeaders: { Authorization: `Bearer ${token}` },
          })
        );
      } else {
        return throwError(() => new Error('Failed to get refreshed token'));
      }
    })
  );
};
