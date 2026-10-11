import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

export const clientGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.user()?.roles.includes('file-owner')) {
    return true;
  }

  router.navigateByUrl('/');
  return false;
};

export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (auth.user()?.roles.includes('file-uploader')) {
    return true;
  }

  router.navigateByUrl('/');
  return false;
};
