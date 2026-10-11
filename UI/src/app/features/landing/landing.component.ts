import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';

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

  readonly user = this.auth.user;

  async loginAsClient(): Promise<void> {
    await this.auth.loginAsClient();
  }

  async loginAsAdmin(): Promise<void> {
    await this.auth.loginAsAdmin();
  }

  async loginWithKeycloak(): Promise<void> {
    await this.auth.loginAsClient();
  }

  async logout(): Promise<void> {
    await this.auth.logout();
  }
}
