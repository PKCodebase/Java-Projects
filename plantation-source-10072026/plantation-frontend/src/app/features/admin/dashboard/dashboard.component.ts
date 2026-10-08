import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { ApiService } from '../../../core/http/api.service';
import { DashboardOverview } from '../../../core/models';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatIconModule, MatTableModule, MatChipsModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css',
})
export class DashboardComponent {
  private readonly api = inject(ApiService);
  readonly data = signal<DashboardOverview | null>(null);
  readonly loading = signal(true);
  readonly error = signal('');

  readonly kpiCols = ['label', 'value'];

  constructor() {
    this.api.dashboard().subscribe({
      next: (d) => { this.data.set(d); this.loading.set(false); },
      error: (e) => { this.error.set(e?.error?.message ?? 'Failed to load dashboard'); this.loading.set(false); },
    });
  }

  /** Simple horizontal bar % relative to the max, used for zone breakdown. */
  barWidth(value: number, max: number): string {
    return max > 0 ? `${Math.round((value / max) * 100)}%` : '0%';
  }

  maxPlanted(): number {
    const z = this.data()?.zoneBreakdown ?? [];
    return Math.max(1, ...z.map(x => x.totalPlanted));
  }

  /** Highest single-day value in the 7-day trend, across both series. */
  maxTrend(): number {
    const t = this.data()?.dailyTrend ?? [];
    return Math.max(1, ...t.map(x => Math.max(x.planted, x.booked)));
  }

  /** Highest pending count among the top pending parks. */
  maxPending(): number {
    const p = this.data()?.topPendingParks ?? [];
    return Math.max(1, ...p.map(x => x.pendingCount));
  }
}
