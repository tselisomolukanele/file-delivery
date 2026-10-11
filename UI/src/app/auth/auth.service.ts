import { Injectable, computed, signal } from '@angular/core';
import Keycloak from 'keycloak-js';


export interface AppUser {
  id: number;
  name: string;
  email: string;
  roles: string[];
  company: string;
  status: string;
  avatar: string;
}

const KEYCLOAK_CONFIG = {
  url: 'http://localhost:8080',
  realm: 'file-delivery',
  clientId: 'file-delivery-ui',
};

const JWT_STORAGE_KEY = 'file-delivery.jwt';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'file-delivery.user';
  private readonly keycloak = new Keycloak(KEYCLOAK_CONFIG);

  readonly users: AppUser[] = [];
  readonly user = signal<AppUser | null>(this.readStoredUser());
  readonly accessToken = signal<string | null>(this.readStoredToken());
  readonly isAuthenticated = computed(() => !!this.user());

  async init(): Promise<void> {
    try {
      const authenticated = await this.keycloak.init({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: `${window.location.origin}/silent-check-sso.html`,
        pkceMethod: 'S256',
        checkLoginIframe: false,
      });

      if (authenticated) {
        this.syncFromKeycloak();
      } else {
        this.user.set(null);
        this.clearStoredToken();
      }

      if (authenticated) {
        // The parsed access token object
        const authObject = this.keycloak.tokenParsed; 
        
        // Extract realm roles safely
        const realmRoles = authObject?.realm_access?.roles || [];
        console.log("User Realm Roles:", realmRoles);

        for (const role of realmRoles) {
          console.log(`User has role: ${role}`);
        }
      }

    } catch (error) {
      console.error('Keycloak initialization failed', error);
      this.user.set(this.readStoredUser());
      this.accessToken.set(this.readStoredToken());
    }
  }

  getAccessToken(): string | null {
    return this.keycloak.token ?? this.readStoredToken();
  }

  getAuthorizationHeader(): Record<string, string> {
    const token = this.getAccessToken();
    return token ? { Authorization: `Bearer ${token}` } : {};
  }

  async logout(): Promise<void> {
    try {
      await this.keycloak.logout({ redirectUri: `${window.location.origin}/` });
    } finally {
      sessionStorage.removeItem(this.storageKey);
      this.clearStoredToken();
      this.user.set(null);
    }
  }

  async login(): Promise<void> {
    const redirectUri = `${window.location.origin}/`;

    await this.keycloak.login({
      redirectUri,
      scope: 'openid profile email',
    });
  }

  private syncFromKeycloak(): void {
    const claims = this.keycloak.tokenParsed ?? {};
    const roleNames = (claims['realm_access'] as { roles?: string[] } | undefined)?.roles ?? [];
    const clientRoles = (claims['resource_access'] as Record<string, { roles?: string[] }> | undefined)?.[KEYCLOAK_CONFIG.clientId]?.roles ?? [];
    const roles = [...roleNames, ...clientRoles];
    const givenName = typeof claims['given_name'] === 'string' ? claims['given_name'] : '';
    const familyName = typeof claims['family_name'] === 'string' ? claims['family_name'] : '';
    const name = [givenName, familyName].filter(Boolean).join(' ') || (typeof claims['preferred_username'] === 'string' ? claims['preferred_username'] : 'Keycloak User');
    const email = typeof claims['email'] === 'string' ? claims['email'] : `${name}@localhost`;
    const token = this.keycloak.token;

    const user: AppUser = {
      id: Number(this.keycloak.subject ?? Date.now()),
      name,
      email,
      roles,
      company: 'Keycloak / Active Directory',
      status: 'Authenticated',
      avatar: name
        .split(' ')
        .map((part) => part[0])
        .slice(0, 2)
        .join('')
        .toUpperCase() || 'KU',
    };

    this.persistUser(user);
    if (token) {
      this.persistToken(token);
    }
  }

  private persistUser(user: AppUser): AppUser {
    this.user.set(user);
    sessionStorage.setItem(this.storageKey, JSON.stringify(user));
    return user;
  }

  private persistToken(token: string): void {
    this.accessToken.set(token);
    sessionStorage.setItem(JWT_STORAGE_KEY, token);
  }

  private clearStoredToken(): void {
    this.accessToken.set(null);
    sessionStorage.removeItem(JWT_STORAGE_KEY);
  }

  private readStoredToken(): string | null {
    const token = sessionStorage.getItem(JWT_STORAGE_KEY);
    return token || null;
  }

  private readStoredUser(): AppUser | null {
    const item = sessionStorage.getItem(this.storageKey);
    if (!item) {
      return null;
    }

    try {
      return JSON.parse(item) as AppUser;
    } catch {
      sessionStorage.removeItem(this.storageKey);
      return null;
    }
  }
}
