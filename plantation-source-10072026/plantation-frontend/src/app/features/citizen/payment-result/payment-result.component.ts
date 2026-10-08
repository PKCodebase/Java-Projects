import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatListModule } from '@angular/material/list';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ApiService } from '../../../core/http/api.service';
import { Booking } from '../../../core/models';

/**
 * Where the MCD payment gateway sends the citizen back
 * (`PG_RETURN_URL` → `/citizen/payment/result?encryptedResponse=…`).
 *
 * The payload is verified server-side with POST /bookings/payment/verify, and
 * the page then shows exactly what the server says the booking became — no
 * optimistic "success" UI before verification.
 */
@Component({
  selector: 'app-payment-result',
  standalone: true,
  imports: [
    CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule,
    MatChipsModule, MatListModule, MatProgressSpinnerModule,
  ],
  templateUrl: './payment-result.component.html',
})
export class PaymentResultComponent {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);

  readonly verifying = signal(true);
  readonly booking = signal<Booking | null>(null);
  readonly error = signal('');
  readonly noPayload = signal(false);

  constructor() {
    const payload = this.route.snapshot.queryParamMap.get('encryptedResponse');

    if (!payload) {
      this.noPayload.set(true);
      this.verifying.set(false);
      return;
    }

    this.api.verifyPayment(payload).subscribe({
      next: (booking) => {
        this.booking.set(booking);
        this.verifying.set(false);
      },
      error: (e) => {
        this.error.set(e?.error?.message
          ?? 'The payment response could not be verified. If you were charged, check My Bookings.');
        this.verifying.set(false);
      },
    });
  }

  get paid(): boolean {
    const status = this.booking()?.status;
    return status === 'PAID' || status === 'SCHEDULED' || status === 'WAITING'
      || status === 'PLANTED' || status === 'COMPLETED';
  }
}
