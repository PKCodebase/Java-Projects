import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, AuthResponse, CitizenRegisterRequest, LoginRequest, OauthConfig } from '../models';
import { TokenStore } from './token.store';

/**
 * Authentication facade.
 *
 * The Spring backend exposes (per SecurityConfig + AuthService contract):
 *   POST /auth/citizen/register
 *   POST /auth/citizen/login
 *   POST /auth/official/login
 *   POST /auth/refresh
 *
 * Forgot-password and OTP login are wired here as well; if the backend build
 * has no channel configured for them, the caller receives a normal HTTP error
 * (501 + an explicit message) which the UI surfaces gracefully. Google sign-in
 * is implemented end to end and enabled only when the backend is configured
 * with GOOGLE_CLIENT_ID — ask it with {@link googleConfig}.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly store = inject(TokenStore);
  private readonly router = inject(Router);
  private readonly base = `${environment.apiUrl}/auth`;

  get isAuthenticated(): boolean { return this.store.isAuthenticated; }
  get role(): string { return this.store.role; }
  get user$() { return this.store.user$; }

  registerCitizen(payload: CitizenRegisterRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.base}/citizen/register`, payload)
      .pipe(tap(res => this.persist(res)));
  }

  loginCitizen(payload: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.base}/citizen/login`, payload)
      .pipe(tap(res => this.persist(res)));
  }

  loginOfficial(payload: LoginRequest): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.base}/official/login`, payload)
      .pipe(tap(res => this.persist(res)));
  }

  // ── Forgot password (email OTP) ──────────────────────────────────
  forgotPassword(email: string): Observable<ApiResponse<unknown>> {
    return this.http.post<ApiResponse<unknown>>(`${this.base}/forgot-password`, { email });
  }

  resetPassword(email: string, otp: string, newPassword: string): Observable<ApiResponse<unknown>> {
    return this.http.post<ApiResponse<unknown>>(`${this.base}/reset-password`,
      { email, otp, newPassword });
  }

  // ── OTP (mobile) login ───────────────────────────────────────────
  sendOtp(mobileNumber: string): Observable<ApiResponse<unknown>> {
    return this.http.post<ApiResponse<unknown>>(`${this.base}/otp/send`, { mobileNumber });
  }

  loginWithOtp(mobileNumber: string, otp: string): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.base}/otp/login`, { mobileNumber, otp })
      .pipe(tap(res => this.persist(res)));
  }

  // ── Google sign-in (OAuth2) ──────────────────────────────────────

  /**
   * Public Google sign-in configuration: `enabled` (backend has GOOGLE_CLIENT_ID
   * set) and the PUBLIC OAuth2 client id used to initialise Google Identity
   * Services. Anonymous endpoint, no token and no secret ever returned.
   */
  googleConfig(): Observable<ApiResponse<OauthConfig>> {
    return this.http.get<ApiResponse<OauthConfig>>(`${this.base}/oauth2/google/config`);
  }

  /** Exchanges the Google ID token (`credential`) for a backend JWT. */
  oauth2Login(credential: string): Observable<ApiResponse<AuthResponse>> {
    return this.http.post<ApiResponse<AuthResponse>>(`${this.base}/oauth2/google`, { credential })
      .pipe(tap(res => this.persist(res)));
  }

  logout(): void {
    this.store.clear();
    this.router.navigate(['/login']);
  }

  private persist(res: ApiResponse<AuthResponse> | AuthResponse): void {
    const auth = (res as ApiResponse<AuthResponse>).data ?? (res as AuthResponse);
    if (auth?.accessToken) {
      this.store.save(auth.accessToken, auth.refreshToken ?? '', auth.user ?? null);
    }
  }
}
