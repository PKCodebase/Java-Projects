import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatStepperModule } from '@angular/material/stepper';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatIconModule, MatStepperModule, MatProgressSpinnerModule, MatSnackBarModule,
  ],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.css',
})
export class ForgotPasswordComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly snack = inject(MatSnackBar);

  readonly loading = signal(false);
  readonly emailSentTo = signal('');

  readonly emailForm = this.fb.nonNullable.group({ email: ['', [Validators.required, Validators.email]] });
  readonly otpForm = this.fb.nonNullable.group({
    otp: ['', [Validators.required, Validators.minLength(4), Validators.maxLength(6)]],
    newPassword: ['', [Validators.required, Validators.minLength(8)]],
  });

  sendOtp(): void {
    if (this.emailForm.invalid) { this.emailForm.markAllAsTouched(); return; }
    this.loading.set(true);
    const email = this.emailForm.getRawValue().email;
    this.auth.forgotPassword(email).subscribe({
      next: () => {
        this.loading.set(false);
        this.emailSentTo.set(email);
        this.snack.open('OTP sent to your email', 'OK', { duration: 3000 });
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Could not send OTP', 'Close', { duration: 4000 });
      },
    });
  }

  reset(): void {
    if (this.otpForm.invalid) { this.otpForm.markAllAsTouched(); return; }
    this.loading.set(true);
    const v = this.otpForm.getRawValue();
    this.auth.resetPassword(this.emailSentTo(), v.otp, v.newPassword).subscribe({
      next: () => {
        this.loading.set(false);
        this.snack.open('Password reset! Please sign in.', 'OK', { duration: 3000 });
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Reset failed', 'Close', { duration: 4000 });
      },
    });
  }
}
