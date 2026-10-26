import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { catchError, throwError } from 'rxjs';

import { AuthService } from '../auth/auth.service';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastr = inject(ToastrService);
  const auth = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const status = error.status;
      console.log(error.error)

      console.log("status: " + status)
      if (status === 401) {
        const tokenExpired = auth.isTokenExpired() || !auth.getAccessToken();

        if (tokenExpired) {
          void auth.refreshSessionIfNeeded().then((refreshed) => {
            if (!refreshed) {
              void auth.redirectToLogin('Your session expired. Please sign in again.');
            }
          });

          toastr.warning('Your session expired. Please sign in again.', 'Session expired', {
            timeOut: 5000,
            closeButton: true,
            progressBar: true,
          });
        }
      }

      if (status >= 400) {
        console.log("again: " + status)
        const message =
          status >= 500
            ? 'System error, please contact support'
            : (typeof error?.error === 'string' ? error?.error : null) ||
              error.error?.message ||
              error.error?.error ||
              error.error?.detail ||
              error.message ||
              'An unexpected error occurred.';

        console.log("wheres my toast: " + status)
        toastr.error(message, `Request failed (${status})`, {
          timeOut: 5000,
          closeButton: true,
          progressBar: true,
        });
      }

      return throwError(() => error);
    })
  );
};
