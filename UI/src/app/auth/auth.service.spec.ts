import '@angular/compiler';
import { BrowserDynamicTestingModule, platformBrowserDynamicTesting } from '@angular/platform-browser-dynamic/testing';
import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';

TestBed.initTestEnvironment(BrowserDynamicTestingModule, platformBrowserDynamicTesting());

vi.mock('keycloak-js', () => ({
  default: class MockKeycloak {
    token: string | null = null;
    subject: string | null = null;
    tokenParsed: Record<string, unknown> | undefined;

    init = vi.fn().mockResolvedValue(false);
    login = vi.fn().mockResolvedValue(undefined);
    logout = vi.fn().mockResolvedValue(undefined);
    updateToken = vi.fn().mockResolvedValue(false);

    constructor(public config?: Record<string, string>) {}
  },
}));

import { AuthService } from './auth.service';

describe('AuthService', () => {
  beforeEach(() => {
    sessionStorage.clear();
  });

  it('reads the persisted user and jwt from session storage on startup', () => {
    sessionStorage.setItem(
      'file-delivery.user',
      JSON.stringify({
        id: 7,
        name: 'Alice Admin',
        email: 'alice@example.com',
        roles: ['file-uploader'],
        company: 'Example',
        status: 'Authenticated',
        avatar: 'AA',
      }),
    );
    sessionStorage.setItem('file-delivery.jwt', 'persisted-token');

    const service = new AuthService();

    expect(service.user()?.name).toBe('Alice Admin');
    expect(service.accessToken()).toBe('persisted-token');
    expect(service.isAuthenticated()).toBe(true);
  });

  it('syncs roles and display name from the Keycloak claims', () => {
    const service = new AuthService();
    const keycloak = (service as any).keycloak;

    keycloak.token = 'jwt-token';
    keycloak.subject = '42';
    keycloak.tokenParsed = {
      given_name: 'Dana',
      family_name: 'Owner',
      realm_access: { roles: ['file-owner'] },
      resource_access: { 'file-delivery-ui': { roles: ['file-uploader'] } },
    };

    (service as any).syncFromKeycloak();

    expect(service.user()).toMatchObject({
      id: 42,
      name: 'Dana Owner',
      email: 'Dana Owner@localhost',
      roles: ['file-owner', 'file-uploader'],
      company: 'Keycloak / Active Directory',
      status: 'Authenticated',
    });
    expect(service.accessToken()).toBe('jwt-token');
    expect(sessionStorage.getItem('file-delivery.user')).not.toBeNull();
  });

  it('marks a token as expired when the encoded timestamp has passed', () => {
    const service = new AuthService();
    const keycloak = (service as any).keycloak;

    keycloak.tokenParsed = { exp: Math.floor(Date.now() / 1000) - 60 };

    expect(service.isTokenExpired()).toBe(true);
  });

  it('clears the session when logging out', async () => {
    const service = new AuthService();
    const keycloak = (service as any).keycloak;

    keycloak.token = 'jwt-token';
    sessionStorage.setItem('file-delivery.user', JSON.stringify({ name: 'Alice', roles: ['file-owner'] }));
    sessionStorage.setItem('file-delivery.jwt', 'jwt-token');

    await service.logout();

    expect(keycloak.logout).toHaveBeenCalledTimes(1);
    expect(service.user()).toBeNull();
    expect(service.accessToken()).toBeNull();
    expect(sessionStorage.getItem('file-delivery.user')).toBeNull();
  });
});
