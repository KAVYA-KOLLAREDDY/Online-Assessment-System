import { CanActivateFn } from '@angular/router';

export const examGuard: CanActivateFn = (route, state) => {
  const resultId = route.paramMap.get('id');
  return true;
};
