import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-otp-login',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatIconModule, MatProgressSpinnerModule, MatSnackBarModule,
  ],
  templateUrl: './otp-login.component.html',
  styleUrl: './otp-login.component.css',
})
export class OtpLoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);

  readonly loading = signal(false);
  readonly otpSent = signal(false);
  readonly mobile = signal('');

  readonly mobileForm = this.fb.nonNullable.group({
    mobileNumber: ['', [Validators.required, Validators.pattern(/^[6-9]\d{9}$/)]],
  });
  readonly otpForm = this.fb.nonNullable.group({
    otp: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(6)]],
  });

  sendOtp(): void {
    if (this.mobileForm.invalid) { this.mobileForm.markAllAsTouched(); return; }
    this.loading.set(true);
    const num = this.mobileForm.getRawValue().mobileNumber;
    this.auth.sendOtp(num).subscribe({
      next: () => {
        this.loading.set(false);
        this.mobile.set(num);
        this.otpSent.set(true);
        this.snack.open('OTP sent to your mobile', 'OK', { duration: 3000 });
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Could not send OTP', 'Close', { duration: 4000 });
      },
    });
  }

  verifyOtp(): void {
    if (this.otpForm.invalid) { this.otpForm.markAllAsTouched(); return; }
    this.loading.set(true);
    this.auth.loginWithOtp(this.mobile(), this.otpForm.getRawValue().otp).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.snack.open('Signed in!', 'OK', { duration: 2500 });
        const role = (res.data?.user?.role ?? '').toUpperCase();
        this.router.navigateByUrl(role.includes('HORTIC') ? '/officer/dashboard' : '/citizen/bookings');
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Invalid OTP', 'Close', { duration: 4000 });
      },
    });
  }
}
