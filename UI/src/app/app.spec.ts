import '@angular/compiler';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';
import { Router } from '@angular/router';

TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());

import { AuthService } from './auth/auth.service';
import { App } from './app';

describe('App', () => {
  const authState = signal<any>(null);
  const authMock = {
    user: authState,
    init: vi.fn().mockResolvedValue(undefined),
    login: vi.fn().mockResolvedValue(undefined),
    logout: vi.fn().mockResolvedValue(undefined),
  };
  const routerMock = { navigateByUrl: vi.fn().mockResolvedValue(true) };

  beforeEach(() => {
    authState.set(null);
    vi.clearAllMocks();

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authMock },
        { provide: Router, useValue: routerMock },
      ],
    });
  });

  it('initializes auth on startup', () => {
    const app = TestBed.runInInjectionContext(() => new App());

    app.ngOnInit();

    expect(authMock.init).toHaveBeenCalledTimes(1);
  });

  it('calls login when the login action is triggered', async () => {
    const app = TestBed.runInInjectionContext(() => new App());

    await app.login();

    expect(authMock.login).toHaveBeenCalledTimes(1);
  });

  it('logs out and redirects to home when the logout action is triggered', async () => {
    const app = TestBed.runInInjectionContext(() => new App());

    await app.logout();

    expect(authMock.logout).toHaveBeenCalledTimes(1);
    expect(routerMock.navigateByUrl).toHaveBeenCalledWith('/');
  });
});
