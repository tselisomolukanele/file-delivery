import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, Observable, throwError } from 'rxjs';
import { ToastrService } from 'ngx-toastr';
import { inject } from '@angular/core';

import { AuthService } from './auth.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private readonly toastr: ToastrService) {}

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    console.error('HTTP Error:');
    return next.handle(req).pipe(
          catchError((error: HttpErrorResponse) => {
            console.error('HTTP Error:', error);
            this.toastr.error('Error', error.message);
            return throwError(() => error);
          })
    );
  }
}
