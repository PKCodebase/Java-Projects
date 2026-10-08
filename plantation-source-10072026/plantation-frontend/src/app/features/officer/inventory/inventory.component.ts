import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatInputModule } from '@angular/material/input';
import { ApiService } from '../../../core/http/api.service';
import { InventoryMatrix, Park } from '../../../core/models';
import { toLocalDate } from '../../../core/util/date.util';

@Component({
  selector: 'app-inventory',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatTableModule, MatCardModule,
    MatFormFieldModule, MatSelectModule, MatDatepickerModule, MatInputModule,
  ],
  templateUrl: './inventory.component.html',
  styleUrl: './inventory.component.css',
})
export class InventoryComponent {
  private readonly api = inject(ApiService);

  readonly myParks = signal<Park[]>([]);
  readonly matrix = signal<InventoryMatrix | null>(null);
  readonly loading = signal(false);

  selectedPark = '';
  date: Date = new Date();
  /** Columns are dynamic (one per species) plus the fixed slot column. */
  cols: string[] = ['slot'];

  constructor() {
    this.api.myParks().subscribe(p => {
      this.myParks.set(p);
      if (p.length) { this.selectedPark = p[0].parkId; this.load(); }
    });
  }

  load(): void {
    if (!this.selectedPark) return;
    this.loading.set(true);
    const d = toLocalDate(this.date);
    this.api.inventoryMatrix(this.selectedPark, d).subscribe({
      next: (m) => {
        this.matrix.set(m);
        this.cols = ['slot', ...(m.speciesNames ?? [])];
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });
  }

  cellValue(row: InventoryMatrix['rows'][number], species: string): string {
    const idx = (this.matrix()?.speciesNames ?? []).indexOf(species);
    if (idx < 0 || !row.cells?.[idx]) return '—';
    const c = row.cells[idx];
    return `${c.availableQty}/${c.stockQty}`;
  }
}
