import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from './auth/auth.service';

@Component({
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App {
  readonly auth = inject(AuthService);
  readonly router = inject(Router);
  readonly user = this.auth.user;

  loginAsClient(): void {
    const user = this.auth.loginAsClient();
    this.router.navigateByUrl(user.role === 'client' ? '/account' : '/');
  }

  loginAsAdmin(): void {
    const user = this.auth.loginAsAdmin();
    this.router.navigateByUrl(user.role === 'admin' ? '/accounts' : '/');
  }

  logout(): void {
    this.auth.logout();
    this.router.navigateByUrl('/');
  }
}
