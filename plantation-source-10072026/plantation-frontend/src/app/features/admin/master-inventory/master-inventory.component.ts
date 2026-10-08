import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/http/api.service';
import { MasterInventory, MasterInventoryHistory, TreeSpecies } from '../../../core/models';

@Component({
  selector: 'app-master-inventory',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatTableModule, MatCardModule, MatButtonModule,
    MatIconModule, MatFormFieldModule, MatSelectModule, MatInputModule,
    MatTabsModule, MatSnackBarModule,
  ],
  templateUrl: './master-inventory.component.html',
  styleUrl: './master-inventory.component.css',
})
export class MasterInventoryComponent {
  private readonly api = inject(ApiService);
  private readonly snack = inject(MatSnackBar);

  readonly stock = signal<MasterInventory[]>([]);
  readonly history = signal<MasterInventoryHistory[]>([]);
  readonly trees = signal<TreeSpecies[]>([]);
  readonly loading = signal(true);

  readonly cols = ['emoji', 'name', 'category', 'total', 'allocated', 'available', 'action'];
  readonly histCols = ['action', 'species', 'qty', 'park', 'by', 'at'];

  selectedSpecies = '';
  qty = 0;

  constructor() {
    this.load();
    this.api.treeSpecies().subscribe(t => this.trees.set(t));
  }

  load(): void {
    this.loading.set(true);
    this.api.masterInventory().subscribe({
      next: (s) => { this.stock.set(s); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
    this.api.masterHistory().subscribe(h => this.history.set(h));
  }

  setStock(): void {
    if (!this.selectedSpecies) return;
    this.api.setMasterStock(this.selectedSpecies, this.qty).subscribe({
      next: () => { this.snack.open('Stock updated', 'OK', { duration: 2000 }); this.load(); },
      error: (e) => this.snack.open(e?.error?.message ?? 'Failed', 'Close', { duration: 3500 }),
    });
  }
}
