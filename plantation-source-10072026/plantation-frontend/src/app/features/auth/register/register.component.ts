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
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatIconModule, MatProgressSpinnerModule, MatSnackBarModule,
  ],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);

  readonly loading = signal(false);
  readonly hide = signal(true);

  readonly form = this.fb.nonNullable.group({
    fullName: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8)]],
    phone: ['', [Validators.pattern(/^[6-9]\d{9}$/)]],
    aadhaarLast4: ['', [Validators.pattern(/^\d{4}$/)]],
    address: [''],
  });

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    const { fullName, email, password, phone } = this.form.getRawValue();
    const aadhaarLast4 = this.form.getRawValue().aadhaarLast4?.trim();
    const address = this.form.getRawValue().address?.trim();
    this.auth.registerCitizen({
      fullName, email, password,
      phone: phone?.trim() || undefined,
      // optional fields: omit when blank so the backend skips its size/pattern checks
      aadhaarLast4: aadhaarLast4 || undefined,
      address: address || undefined,
    }).subscribe({
      next: (res) => {
        this.loading.set(false);
        this.snack.open('Account created successfully!', 'OK', { duration: 2500 });
        const role = res.data?.user?.role ?? '';
        this.router.navigateByUrl(role.toUpperCase().includes('HORTIC') ? '/officer/dashboard' : '/citizen/bookings');
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(err?.error?.message ?? 'Registration failed', 'Close', { duration: 4000 });
      },
    });
  }
}
