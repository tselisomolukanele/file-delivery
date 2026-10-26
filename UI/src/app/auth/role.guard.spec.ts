import '@angular/compiler';
import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';
import { Router } from '@angular/router';

TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());

import { AuthService } from './auth.service';
import { adminGuard, clientGuard } from './role.guard';

describe('role guards', () => {
  const authState = signal<any>(null);

  beforeEach(() => {
    authState.set(null);
    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: { user: authState } },
        { provide: Router, useValue: { navigateByUrl: vi.fn() } },
      ],
    });
  });

  it('allows file owner accounts through the client guard', () => {
    authState.set({ roles: ['file-owner'] });

    const router = TestBed.inject(Router);
    const result = TestBed.runInInjectionContext(() => clientGuard({} as any, {} as any));

    expect(result).toBe(true);
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('redirects users without client access to the landing page', () => {
    authState.set({ roles: ['file-uploader'] });

    const router = TestBed.inject(Router);
    const result = TestBed.runInInjectionContext(() => clientGuard({} as any, {} as any));

    expect(result).toBe(false);
    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
  });

  it('allows uploader roles through the admin guard', () => {
    authState.set({ roles: ['file-uploader'] });

    const router = TestBed.inject(Router);
    const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));

    expect(result).toBe(true);
    expect(router.navigateByUrl).not.toHaveBeenCalled();
  });

  it('redirects non-admin users away from the admin route', () => {
    authState.set({ roles: ['file-owner'] });

    const router = TestBed.inject(Router);
    const result = TestBed.runInInjectionContext(() => adminGuard({} as any, {} as any));

    expect(result).toBe(false);
    expect(router.navigateByUrl).toHaveBeenCalledWith('/');
  });
});
