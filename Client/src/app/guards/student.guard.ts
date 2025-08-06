import { computed, inject } from '@angular/core';
import { CanActivateChildFn, CanActivateFn } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { AuthService } from '../service/Auth.service';

export const studentGuard: CanActivateFn = (childRoute, state) => {
  const authService = inject(AuthService);
  let user = computed(() => authService.currentUser());
  if (user() != null) {
    console.log(user())

    if (user().authorities === 'ROLE_STUDENT') {
      return true;
    }
  }
  alert('ACCESS DENIED!');
  return false;
};

export const studentChildGuard: CanActivateChildFn = (route, state) => {
  return studentGuard(route, state);
};
