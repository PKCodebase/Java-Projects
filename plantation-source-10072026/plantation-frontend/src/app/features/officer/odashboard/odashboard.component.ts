import { Component, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { catchError, forkJoin, Observable, of } from 'rxjs';
import { ApiService } from '../../../core/http/api.service';
import { TokenStore } from '../../../core/auth/token.store';
import {
  Booking, BookingStatus, CompletedPlantation, LowStockAlert, Park,
} from '../../../core/models';

/** Bookings awaiting plantation action (matches the backend's active set). */
const ACTIVE: BookingStatus[] = ['PAID', 'SCHEDULED', 'WAITING'];

/**
 * Individual officer dashboard.
 *
 * Every number comes from a backend endpoint that is ALREADY scoped to the
 * logged-in official by `userSystemCode`:
 *   GET /mcd/my-parks                 → parks assigned to me (admin → all parks)
 *   GET /mcd/plantation/pending       → PAID/SCHEDULED/WAITING in my parks
 *   GET /mcd/plantation/pendingToday  → same, filtered to today
 *   GET /mcd/plantation/completed     → plantation records in my parks
 *   GET /mcd/inventory/low-stock      → low-stock rows in my parks
 * Nothing here is a global figure, so an officer never sees another zone's data.
 */
@Component({
  selector: 'app-odashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatIconModule, MatButtonModule],
  templateUrl: './odashboard.component.html',
  styleUrl: './odashboard.component.css',
})
export class ODashboardComponent {
  private readonly api = inject(ApiService);
  private readonly store = inject(TokenStore);

  readonly parks = signal<Park[]>([]);
  readonly pending = signal<Booking[]>([]);
  readonly today = signal<Booking[]>([]);
  readonly completed = signal<CompletedPlantation[]>([]);
  readonly lowStock = signal<LowStockAlert[]>([]);
  readonly loading = signal(true);
  readonly error = signal('');

  readonly user = this.store.user;
  readonly roleLabel = this.labelFor(this.store.role);

  private labelFor(role: string): string {
    switch (role) {
      case 'R_HORTIC_ADM': return 'Horticulture Admin';
      case 'R_HORTIC_OFF': return 'Horticulture Officer';
      case 'SUPERVISOR': return 'Supervisor';
      default: return role || 'Staff';
    }
  }

  /** Distinct zones the officer's parks belong to — their working territory. */
  readonly zones = computed(() => {
    const names = this.parks()
      .map(p => p.zone?.name)
      .filter((n): n is string => !!n);
    return [...new Set(names)];
  });

  readonly treesToPlant = computed(() => this.sumTrees(this.pending()));
  readonly completedThisMonth = computed(() => {
    const now = new Date();
    const prefix = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    return this.completed().filter(c => (c.plantedDate ?? '').startsWith(prefix));
  });

  /** Pending work sorted by slot date, oldest first (most overdue on top). */
  readonly pendingSorted = computed(() =>
    [...this.pending()].sort((a, b) => (a.slotDate ?? '').localeCompare(b.slotDate ?? '')));

  readonly recentCompleted = computed(() =>
    [...this.completed()]
      .sort((a, b) => (b.plantedDate ?? b.recordedAt ?? '').localeCompare(a.plantedDate ?? a.recordedAt ?? ''))
      .slice(0, 6));

  /** True when the backend scoped this officer to zero parks. */
  readonly noScope = computed(() =>
    this.parks().length === 0 && this.store.role !== 'R_HORTIC_ADM');

  constructor() {
    // Each call is isolated so one failing endpoint cannot blank the dashboard.
    forkJoin({
      parks: this.scoped(this.api.myParks(), []),
      pending: this.scoped(this.api.pendingPlantations(), []),
      today: this.scoped(this.api.pendingToday(), []),
      completed: this.scoped(this.api.completedPlantations(), []),
      lowStock: this.scoped(this.api.lowStock(), []),
    }).subscribe({
      next: (r) => {
        this.parks.set(r.parks);
        this.pending.set(r.pending.filter(b => ACTIVE.includes(b.status)));
        this.today.set(r.today.filter(b => ACTIVE.includes(b.status)));
        this.completed.set(r.completed);
        this.lowStock.set(r.lowStock);
        this.loading.set(false);
      },
      error: (e) => {
        this.error.set(e?.error?.message ?? 'Could not load your dashboard');
        this.loading.set(false);
      },
    });
  }

  private scoped<T>(source: Observable<T>, fallback: T): Observable<T> {
    return source.pipe(catchError(() => of(fallback)));
  }

  private sumTrees(list: Booking[]): number {
    return list.reduce(
      (sum, b) => sum + (b.items ?? []).reduce((s, i) => s + (i.quantity ?? 0), 0),
      0);
  }

  treeCount(b: Booking): number {
    return (b.items ?? []).reduce((s, i) => s + (i.quantity ?? 0), 0);
  }

  statusClass(s: string): string {
    return 'st-' + s.toLowerCase().replace(/_/g, '-');
  }

  statusLabel(s: string): string {
    return s.replace(/_/g, ' ').toLowerCase().replace(/\b\w/g, c => c.toUpperCase());
  }
}
