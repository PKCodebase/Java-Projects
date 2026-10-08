import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { UserSummary } from '../models';

const KEY_TOKEN = 'pl_access_token';
const KEY_REFRESH = 'pl_refresh_token';
const KEY_USER = 'pl_user';

/**
 * Client-side session store. Tokens are kept in sessionStorage (cleared on tab
 * close) rather than localStorage. No secret is ever written to source code.
 */
@Injectable({ providedIn: 'root' })
export class TokenStore {
  private readonly userSubject = new BehaviorSubject<UserSummary | null>(this.readUser());
  readonly user$: Observable<UserSummary | null> = this.userSubject.asObservable();

  get token(): string | null { return sessionStorage.getItem(KEY_TOKEN); }
  get refreshToken(): string | null { return sessionStorage.getItem(KEY_REFRESH); }
  get user(): UserSummary | null { return this.userSubject.value; }

  get isAuthenticated(): boolean { return !!this.token; }

  save(access: string, refresh: string, user: UserSummary | null): void {
    sessionStorage.setItem(KEY_TOKEN, access);
    sessionStorage.setItem(KEY_REFRESH, refresh);
    if (user) {
      sessionStorage.setItem(KEY_USER, JSON.stringify(user));
      this.userSubject.next(user);
    }
  }

  clear(): void {
    sessionStorage.removeItem(KEY_TOKEN);
    sessionStorage.removeItem(KEY_REFRESH);
    sessionStorage.removeItem(KEY_USER);
    this.userSubject.next(null);
  }

  /** Role without the Spring `ROLE_` prefix, upper-cased. */
  get role(): string {
    const u = this.userSubject.value;
    if (!u?.role) return '';
    return u.role.replace(/^ROLE_/i, '').toUpperCase();
  }

  private readUser(): UserSummary | null {
    try {
      const raw = sessionStorage.getItem(KEY_USER);
      return raw ? (JSON.parse(raw) as UserSummary) : null;
    } catch { return null; }
  }
}
