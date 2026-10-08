import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { ApiService } from '../../../core/http/api.service';
import { LowStockAlert } from '../../../core/models';

@Component({
  selector: 'app-low-stock',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatCardModule, MatIconModule],
  templateUrl: './low-stock.component.html',
  styleUrl: './low-stock.component.css',
})
export class LowStockComponent {
  private readonly api = inject(ApiService);
  readonly alerts = signal<LowStockAlert[]>([]);
  readonly loading = signal(true);
  readonly cols = ['tree', 'park', 'slot', 'stock', 'reserved', 'available'];

  constructor() {
    this.api.lowStock().subscribe({
      next: (a) => { this.alerts.set(a); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
}
