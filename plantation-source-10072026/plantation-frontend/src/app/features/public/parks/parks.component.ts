import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';
import { ApiService } from '../../../core/http/api.service';
import { Park, Zone } from '../../../core/models';

@Component({
  selector: 'app-parks',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink, MatCardModule, MatFormFieldModule,
    MatSelectModule, MatPaginatorModule, MatChipsModule,
  ],
  templateUrl: './parks.component.html',
  styleUrl: './parks.component.css',
})
export class ParksComponent {
  private readonly api = inject(ApiService);

  readonly parks = signal<Park[]>([]);
  readonly zones = signal<Zone[]>([]);
  readonly total = signal(0);
  readonly loading = signal(true);
  selectedZone = '';

  readonly pageSize = 9;
  pageIndex = 0;

  constructor() {
    this.api.zones().subscribe(z => this.zones.set(z));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.api.parks(this.selectedZone || undefined, this.pageIndex, this.pageSize).subscribe({
      next: (res) => {
        this.parks.set(res.content ?? []);
        this.total.set(res.totalElements ?? 0);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  onZoneChange(): void { this.pageIndex = 0; this.load(); }
  onPage(e: PageEvent): void { this.pageIndex = e.pageIndex; this.load(); }
}
