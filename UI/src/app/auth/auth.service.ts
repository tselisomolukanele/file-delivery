import { Injectable, computed, signal } from '@angular/core';

export type UserRole = 'client' | 'admin';

export interface AppUser {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  company: string;
  status: string;
  avatar: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly storageKey = 'file-delivery.user';

  readonly users: AppUser[] = [
    {
      id: 101,
      name: 'Ava Thompson',
      email: 'ava.thompson@gmail.com',
      role: 'client',
      company: 'Northstar Logistics',
      status: 'Active',
      avatar: 'AT',
    },
    {
      id: 202,
      name: 'Marcus Lee',
      email: 'marcus.lee@filedelivery.com',
      role: 'admin',
      company: 'FileDelivery Admin',
      status: 'Owner',
      avatar: 'ML',
    },
    {
      id: 303,
      name: 'Priya Nair',
      email: 'priya.nair@filedelivery.com',
      role: 'admin',
      company: 'FileDelivery Admin',
      status: 'Operations',
      avatar: 'PN',
    },
    {
      id: 404,
      name: 'Daniel Brooks',
      email: 'daniel.brooks@gmail.com',
      role: 'client',
      company: 'Brookside Studio',
      status: 'Active',
      avatar: 'DB',
    },
  ];

  readonly user = signal<AppUser | null>(this.readStoredUser());
  readonly isAuthenticated = computed(() => !!this.user());

  loginAsClient(): AppUser {
    const user = this.users.find((item) => item.role === 'client') ?? this.users[0];
    return this.persistUser(user);
  }

  loginAsAdmin(): AppUser {
    const user = this.users.find((item) => item.role === 'admin') ?? this.users[1];
    return this.persistUser(user);
  }

  loginWithOAuth2(provider: 'google' | 'github', role: UserRole): AppUser {
    const matched = this.users.find((user) => user.role === role && user.email.includes(provider === 'google' ? 'gmail.com' : 'filedelivery.com'));
    const fallback = this.users.find((user) => user.role === role) ?? this.users[0];
    return this.persistUser(matched ?? fallback);
  }

  logout(): void {
    sessionStorage.removeItem(this.storageKey);
    this.user.set(null);
  }

  private persistUser(user: AppUser): AppUser {
    this.user.set(user);
    sessionStorage.setItem(this.storageKey, JSON.stringify(user));
    return user;
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
