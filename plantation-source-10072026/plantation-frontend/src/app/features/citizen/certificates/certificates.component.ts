import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ApiService } from '../../../core/http/api.service';
import { Certificate } from '../../../core/models';

@Component({
  selector: 'app-certificates',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './certificates.component.html',
  styleUrl: './certificates.component.css',
})
export class CertificatesComponent {
  private readonly api = inject(ApiService);
  readonly certs = signal<Certificate[]>([]);
  readonly loading = signal(true);

  constructor() {
    // Certificates are resolved per booking; load all bookings then flatten.
    this.api.myBookings().subscribe({
      next: (bookings) => {
        const done = bookings.filter(b => ['PLANTED', 'COMPLETED'].includes(b.status));
        if (!done.length) { this.loading.set(false); return; }
        let pending = done.length;
        done.forEach(b => {
          this.api.certificatesForBooking(b.bookingId).subscribe({
            next: (list) => {
              this.certs.update(c => [...c, ...list]);
              if (--pending === 0) this.sortAndStop();
            },
            error: () => { if (--pending === 0) this.sortAndStop(); },
          });
        });
      },
      error: () => this.loading.set(false),
    });
  }

  private sortAndStop(): void {
    this.certs.update(c => c.sort((a, b) =>
      (b.issuedAt ?? '').localeCompare(a.issuedAt ?? '')));
    this.loading.set(false);
  }
}
