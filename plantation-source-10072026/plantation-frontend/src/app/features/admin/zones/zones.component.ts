import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ApiService } from '../../../core/http/api.service';
import { Zone } from '../../../core/models';

@Component({
  selector: 'app-zones',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './zones.component.html',
  styleUrl: './zones.component.css',
})
export class ZonesComponent {
  private readonly api = inject(ApiService);
  readonly zones = signal<Zone[]>([]);
  readonly loading = signal(true);
  readonly cols = ['code', 'name', 'status'];

  constructor() {
    this.api.zones().subscribe({
      next: (z) => { this.zones.set(z); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
}
