import { CommonModule } from '@angular/common';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ToastrModule } from 'ngx-toastr';

import { ErrorInterceptor } from './auth/error.interceptor';

@NgModule({
  imports: [CommonModule, RouterModule, ToastrModule.forRoot()],
  exports: [CommonModule, RouterModule],
  providers: [{ provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true }],
})
export class AppModule {}
