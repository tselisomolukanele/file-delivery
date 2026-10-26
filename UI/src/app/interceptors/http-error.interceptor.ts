import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { ToastrService } from 'ngx-toastr';
import { catchError, throwError } from 'rxjs';

export const httpErrorInterceptor: HttpInterceptorFn = (req, next) => {
  const toastr = inject(ToastrService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      const status = error.status;
      console.log(error.error)
      if (status >= 400) {
        const message =
        (status >= 500) ? 'System error, please contact support' :
          (typeof error?.error === 'string' ? error?.error : null) ||
          error.error?.message ||
          error.error?.error ||
          error.error?.detail ||
          error.message ||
          'An unexpected error occurred.';

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
