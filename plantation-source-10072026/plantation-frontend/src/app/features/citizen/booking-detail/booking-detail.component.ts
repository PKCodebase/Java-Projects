import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/http/api.service';
import { Booking, PresenceMode } from '../../../core/models';

@Component({
  selector: 'app-booking-detail',
  standalone: true,
  imports: [
    CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule,
    MatChipsModule, MatDividerModule, MatButtonToggleModule, MatSnackBarModule,
    MatListModule,
  ],
  templateUrl: './booking-detail.component.html',
  styleUrl: './booking-detail.component.css',
})
export class BookingDetailComponent {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);

  readonly booking = signal<Booking | null>(null);
  readonly loading = signal(true);
  readonly busy = signal(false);

  constructor() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.api.booking(id).subscribe({
      next: (b) => { this.booking.set(b); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  setPresence(mode: PresenceMode): void {
    const b = this.booking();
    if (!b) return;
    this.api.setPresence(b.bookingId, mode).subscribe({
      next: (updated) => this.booking.set(updated),
      error: (e) => this.snack.open(e?.error?.message ?? 'Failed', 'Close', { duration: 3000 }),
    });
  }

  pay(): void {
    const b = this.booking();
    if (!b) return;
    this.busy.set(true);
    this.api.initPayment(b.bookingId).subscribe({
      next: (res) => {
        this.busy.set(false);
        if (res?.url) {
          // Hand-off to the MCD gateway; the return lands on /citizen/payment/result.
          window.location.assign(res.url);
        } else {
          this.snack.open(res?.message ?? 'The gateway returned no payment URL', 'Close',
            { duration: 5000 });
        }
      },
      error: (e) => {
        this.busy.set(false);
        this.snack.open(e?.error?.message ?? 'Could not start payment', 'Close', { duration: 3500 });
      },
    });
  }

  cancel(): void {
    const b = this.booking();
    if (!b || !confirm('Cancel this booking?')) return;
    this.api.cancelBooking(b.bookingId).subscribe({
      next: () => { this.snack.open('Booking cancelled', 'OK', { duration: 2500 }); this.router.navigate(['/citizen/bookings']); },
      error: (e) => this.snack.open(e?.error?.message ?? 'Failed', 'Close', { duration: 3500 }),
    });
  }
}
