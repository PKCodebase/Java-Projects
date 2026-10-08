import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/http/api.service';
import { Booking } from '../../../core/models';

/**
 * LOCAL TEST GATEWAY — reachable only while the backend runs with
 * PG_TEST_MODE=true, which makes payment/init hand citizens this page instead
 * of the real MCD gateway URL.
 *
 * The two buttons mint a clearly-labelled fake gateway response
 * (POST /bookings/{id}/payment/test-payload) that is then consumed by the
 * REAL /bookings/payment/verify endpoint, so the page shows exactly what the
 * server decided — success and failure both exercise the production verify
 * path, never an optimistic fake "paid" UI. With test mode off the backend
 * answers 404 and this page says so plainly.
 */
@Component({
  selector: 'app-payment-test',
  standalone: true,
  imports: [
    CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule,
    MatListModule, MatChipsModule, MatProgressBarModule, MatSnackBarModule,
  ],
  templateUrl: './payment-test.component.html',
})
export class PaymentTestComponent {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);

  readonly loading = signal(true);
  readonly busy = signal(false);
  readonly booking = signal<Booking | null>(null);
  readonly error = signal('');

  constructor() {
    const bookingId = this.route.snapshot.queryParamMap.get('bookingId');
    const ref = this.route.snapshot.queryParamMap.get('ref');

    if (!bookingId) {
      this.error.set('No booking was passed to the test gateway — open it again from My Bookings.');
      this.loading.set(false);
      return;
    }

    this.api.booking(bookingId).subscribe({
      next: (b) => {
        this.booking.set(b);
        this.loading.set(false);
      },
      error: () => {
        this.error.set(ref
          ? `Booking ${ref} could not be loaded. Is the backend running with PG_TEST_MODE=true?`
          : 'The booking could not be loaded.');
        this.loading.set(false);
      },
    });
  }

  /**
   * Mint the fake gateway response, then hand it to the real result page,
   * which verifies it server-side exactly like a gateway return would.
   */
  pay(result: 'success' | 'failure'): void {
    const b = this.booking();
    if (!b || this.busy()) return;
    this.busy.set(true);

    this.api.testPaymentPayload(b.bookingId, result).subscribe({
      next: (payload) =>
        this.router.navigate(['/citizen/payment/result'], { queryParams: { encryptedResponse: payload } }),
      error: (e) => {
        this.busy.set(false);
        this.error.set(e?.error?.message
          ?? 'The backend refused the test payload — it is probably running with PG_TEST_MODE off.');
        this.snack.open('Test gateway unavailable', 'Dismiss', { duration: 3000 });
      },
    });
  }
}
