import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../../core/http/api.service';
import { TokenStore } from '../../../core/auth/token.store';
import { Booking, BookingStatus } from '../../../core/models';

/** Bookings that are still going to happen (they occupy a slot). */
const UPCOMING: BookingStatus[] = ['PAID', 'SCHEDULED', 'WAITING'];
/** Bookings where the officer already recorded the plantation. */
const PLANTED: BookingStatus[] = ['PLANTED', 'COMPLETED'];

/**
 * Citizen home — everything here is derived from GET /bookings/mine plus the
 * per-booking GET /certificates/{bookingId} calls the certificates page already
 * uses, so the numbers always match what "My Bookings"/"My Certificates" show.
 */
@Component({
  selector: 'app-citizen-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatIconModule, MatButtonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class CitizenDashboardComponent {
  private readonly api = inject(ApiService);
  private readonly store = inject(TokenStore);

  readonly bookings = signal<Booking[]>([]);
  readonly certCount = signal(0);
  readonly loading = signal(true);
  readonly error = signal('');

  readonly total = computed(() => this.bookings().length);

  readonly upcoming = computed(() =>
    this.bookings()
      .filter(b => UPCOMING.includes(b.status))
      .sort((a, b) => (a.slotDate ?? '').localeCompare(b.slotDate ?? '')));

  readonly planted = computed(() =>
    this.bookings().filter(b => PLANTED.includes(b.status)));

  readonly cancelled = computed(() =>
    this.bookings().filter(b => b.status === 'CANCELLED').length);

  readonly treesPlanted = computed(() => this.sumTrees(this.planted()));
  readonly treesReserved = computed(() => this.sumTrees(this.upcoming()));

  /** Every non-zero booking status, in lifecycle order, for the breakdown. */
  readonly statusBreakdown = computed(() => {
    const order: BookingStatus[] = [
      'PENDING_PAYMENT', 'PAID', 'SCHEDULED', 'WAITING',
      'PLANTED', 'COMPLETED', 'CANCELLED',
    ];
    const all = this.bookings();
    return order
      .map(s => ({ status: s, count: all.filter(b => b.status === s).length }))
      .filter(x => x.count > 0);
  });

  /** Newest first, for the "recent bookings" list. */
  readonly recent = computed(() =>
    [...this.bookings()]
      .sort((a, b) => (b.bookedAt ?? '').localeCompare(a.bookedAt ?? ''))
      .slice(0, 5));

  readonly firstName = computed(() =>
    (this.store.user?.fullName ?? '').trim().split(/\s+/)[0] || 'there');

  constructor() {
    this.api.myBookings().subscribe({
      next: (bookings) => {
        this.bookings.set(bookings);
        this.loading.set(false);
        this.loadCertificateCount(bookings);
      },
      error: (e) => {
        this.error.set(e?.error?.message ?? 'Could not load your bookings');
        this.loading.set(false);
      },
    });
  }

  /** Trees booked in a set of bookings (items may be absent on old rows). */
  private sumTrees(list: Booking[]): number {
    return list.reduce(
      (sum, b) => sum + (b.items ?? []).reduce((s, i) => s + (i.quantity ?? 0), 0),
      0);
  }

  /**
   * Certificate count = the same records "My Certificates" lists. Only bookings
   * that are planted/completed can hold one; capped so a huge history cannot
   * fan out into hundreds of parallel requests.
   */
  private loadCertificateCount(bookings: Booking[]): void {
    const ids = bookings
      .filter(b => PLANTED.includes(b.status))
      .sort((a, b) => (b.bookedAt ?? '').localeCompare(a.bookedAt ?? ''))
      .slice(0, 30)
      .map(b => b.bookingId);

    // forkJoin([]) never emits, so short-circuit the empty case.
    if (ids.length === 0) { this.certCount.set(0); return; }

    forkJoin(ids.map(id => this.api.certificatesForBooking(id))).subscribe({
      next: (lists) => this.certCount.set(lists.flat().length),
      error: () => this.certCount.set(0),
    });
  }

  statusClass(s: string): string {
    return 'st-' + s.toLowerCase().replace(/_/g, '-');
  }

  statusLabel(s: string): string {
    return s.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
  }

  treeCount(b: Booking): number {
    return (b.items ?? []).reduce((s, i) => s + (i.quantity ?? 0), 0);
  }
}
