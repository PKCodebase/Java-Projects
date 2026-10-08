import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ApiService } from '../../../core/http/api.service';
import { Booking } from '../../../core/models';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [CommonModule, RouterLink, MatTableModule, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './my-bookings.component.html',
  styleUrl: './my-bookings.component.css',
})
export class MyBookingsComponent {
  private readonly api = inject(ApiService);
  readonly bookings = signal<Booking[]>([]);
  readonly loading = signal(true);
  readonly cols = ['ref', 'park', 'date', 'trees', 'status', 'actions'];

  constructor() {
    this.api.myBookings().subscribe({
      next: (b) => { this.bookings.set(b); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  statusClass(s: string): string {
    return 'st-' + s.toLowerCase().replace(/_/g, '-');
  }
}
