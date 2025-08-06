import { computed, inject } from '@angular/core';
import { CanActivateChildFn, CanActivateFn } from '@angular/router';
import { jwtDecode } from 'jwt-decode';
import { AuthService } from '../service/Auth.service';

export const adminGuard: CanActivateFn = (childRoute, state) => {
const authService = inject(AuthService);
  let user = computed(() => authService.currentUser());
  if (user() != null) {
    console.log(user())

    if (user().authorities === 'ROLE_ADMIN') {
      return true;
    }
  }
  alert('ACCESS DENIED!');
  return false;
}
export const adminChildGuard: CanActivateChildFn = (route, state) => {
  return adminGuard(route, state);
};
