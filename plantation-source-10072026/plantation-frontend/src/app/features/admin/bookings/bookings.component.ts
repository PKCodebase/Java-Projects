import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatChipsModule } from '@angular/material/chips';
import { ApiService } from '../../../core/http/api.service';
import { Booking } from '../../../core/models';

@Component({
  selector: 'app-bookings',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatCardModule, MatButtonModule, MatChipsModule],
  templateUrl: './bookings.component.html',
  styleUrl: './bookings.component.css',
})
export class BookingsComponent {
  private readonly api = inject(ApiService);
  readonly bookings = signal<Booking[]>([]);
  readonly loading = signal(true);
  readonly cols = ['ref', 'citizen', 'park', 'date', 'status', 'amount'];

  constructor() {
    // Admins reuse the citizen "mine" listing when supported; falls back gracefully.
    this.api.myBookings().subscribe({
      next: (b) => { this.bookings.set(b); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
}
