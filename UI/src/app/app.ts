import { CommonModule } from '@angular/common';
import { Component, inject, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { AuthService } from './auth/auth.service';

@Component({
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  selector: 'app-root',
  styleUrl: './app.scss',
  templateUrl: './app.html',
})
export class App implements OnInit {
  readonly auth = inject(AuthService);
  readonly router = inject(Router);
  readonly user = this.auth.user;

  ngOnInit(): void {
    void this.auth.init();
  }

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
    await this.router.navigateByUrl('/');
  }
}
