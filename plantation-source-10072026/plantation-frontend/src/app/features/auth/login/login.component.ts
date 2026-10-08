import { AfterViewInit, Component, ElementRef, ViewChild, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatTabsModule } from '@angular/material/tabs';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/auth/auth.service';
import { GoogleSignInService } from '../../../core/auth/google-sign-in.service';
import { OauthConfig } from '../../../core/models';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatTabsModule, MatIconModule, MatDividerModule,
    MatProgressSpinnerModule, MatSnackBarModule,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent implements AfterViewInit {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);
  private readonly googleSignIn = inject(GoogleSignInService);

  readonly loading = signal(false);
  readonly hide = signal(true);

  /**
   * Google sign-in configuration as reported by the backend. `null` until it
   * answers (and stays `null` if the backend is an older build without the
   * endpoint) — either way the UI shows "not configured", never a dead link.
   */
  readonly googleConfig = signal<OauthConfig | null>(null);

  @ViewChild('googleButton', { static: true })
  private googleButton!: ElementRef<HTMLDivElement>;

  private googleRendered = false;

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  constructor() {
    this.loadGoogleConfig();
  }

  ngAfterViewInit(): void {
    this.renderGoogleIfReady();
  }

  /** Google sign-in is usable only when the backend says so AND gave a client id. */
  get googleEnabled(): boolean {
    const cfg = this.googleConfig();
    return !!(cfg?.enabled && cfg.clientId);
  }

  submit(role: 'citizen' | 'official'): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    const creds = this.form.getRawValue();
    const call = role === 'citizen' ? this.auth.loginCitizen(creds) : this.auth.loginOfficial(creds);

    call.subscribe({
      next: (res) => {
        this.loading.set(false);
        this.snack.open(`Welcome, ${res.data?.user?.fullName ?? ''}!`, 'OK', { duration: 2500 });
        this.router.navigateByUrl(this.homeFor(res.data?.user?.role ?? ''));
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Login failed. Please try again.', 'Close', { duration: 4000 });
      },
    });
  }

  /**
   * Fallback for deployments where GOOGLE_CLIENT_ID is not set: explains what
   * to configure instead of navigating to a URL the backend does not serve.
   */
  oauthLogin(): void {
    this.snack.open(
      'Google sign-in is not configured on this deployment. Set the GOOGLE_CLIENT_ID environment variable on the backend and restart it.',
      'Close',
      { duration: 7000 },
    );
  }

  private loadGoogleConfig(): void {
    this.auth.googleConfig().subscribe({
      next: (res) => {
        this.googleConfig.set(res.data ?? null);
        this.renderGoogleIfReady();
      },
      // Older backend / offline: keep the explained fallback, no external script.
      error: () => this.googleConfig.set(null),
    });
  }

  private renderGoogleIfReady(): void {
    const cfg = this.googleConfig();
    if (this.googleRendered || !cfg?.enabled || !cfg.clientId) return;
    if (!this.googleButton?.nativeElement) return;

    this.googleRendered = true;
    this.googleSignIn
      .renderButton(this.googleButton.nativeElement, cfg.clientId, (credential) => this.googleLogin(credential))
      .catch((err) => {
        this.googleRendered = false;
        this.snack.open(err?.message ?? 'Google sign-in could not be started.', 'Close', { duration: 5000 });
      });
  }

  /** Exchanges the Google ID token for a backend JWT and enters the portal. */
  private googleLogin(credential: string): void {
    this.loading.set(true);
    this.auth.oauth2Login(credential).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.snack.open(`Welcome, ${res.data?.user?.fullName ?? ''}!`, 'OK', { duration: 2500 });
        this.router.navigateByUrl(this.homeFor(res.data?.user?.role ?? 'CITIZEN'));
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Google sign-in failed. Please try again.', 'Close', { duration: 5000 });
      },
    });
  }

  private homeFor(role: string): string {
    const r = role.replace(/^ROLE_/i, '').toUpperCase();
    if (r === 'R_HORTIC_ADM' || r === 'ADMIN') return '/admin/dashboard';
    if (r === 'R_HORTIC_OFF' || r === 'SUPERVISOR') return '/officer/dashboard';
    return '/citizen/bookings';
  }
}
