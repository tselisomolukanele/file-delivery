import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../auth/auth.service';

@Component({
  selector: 'app-landing',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './landing.component.html',
  styleUrl: './landing.component.scss',
})
export class LandingComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly user = this.auth.user;

  loginAsClient(): void {
    const user = this.auth.loginAsClient();
    this.router.navigateByUrl(user.role === 'client' ? '/account' : '/');
  }

  loginAsAdmin(): void {
    const user = this.auth.loginAsAdmin();
    this.router.navigateByUrl(user.role === 'admin' ? '/accounts' : '/');
  }

  loginWithGoogle(): void {
    this.auth.loginWithOAuth2('google', 'client');
    this.router.navigateByUrl('/account');
  }

  logout(): void {
    this.auth.logout();
  }
}
