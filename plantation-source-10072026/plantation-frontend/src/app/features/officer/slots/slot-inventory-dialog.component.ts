import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Observable } from 'rxjs';
import { ApiService } from '../../../core/http/api.service';
import { ParkSlot, SlotInventoryItem, TreeSpecies } from '../../../core/models';

export interface SlotInventoryDialogData { slot: ParkSlot; }

/**
 * Officer dialog that assigns tree species to a plantation slot.
 *
 * Wraps the three slot-inventory endpoints the backend already exposes but the
 * UI never used:
 *   POST   /mcd/inventory           add a species (stock qty) to the slot
 *   PUT    /mcd/inventory/{id}      change its stock qty
 *   DELETE /mcd/inventory/{id}      remove it (blocked while reservations exist)
 *
 * Every change re-reads GET /slots/{id} so stock/reserved/free stay truthful.
 * Backend rules are surfaced verbatim via the snackbar (species must be
 * allocated to the park first, total stock <= slot capacity, stock >= reserved).
 */
@Component({
  selector: 'app-slot-inventory-dialog',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule, MatDialogModule,
    MatFormFieldModule, MatSelectModule, MatInputModule, MatButtonModule,
    MatIconModule, MatSnackBarModule,
  ],
  templateUrl: './slot-inventory-dialog.component.html',
  styleUrl: './slot-inventory-dialog.component.css',
})
export class SlotInventoryDialogComponent {
  private readonly api = inject(ApiService);
  private readonly fb = inject(FormBuilder);
  private readonly snack = inject(MatSnackBar);
  private readonly ref = inject(MatDialogRef<SlotInventoryDialogComponent>);

  /** Slot handed over by the list; refreshed from the API after each change. */
  readonly slot = signal<ParkSlot>(inject<SlotInventoryDialogData>(MAT_DIALOG_DATA).slot);
  readonly species = signal<TreeSpecies[]>([]);
  readonly busy = signal(false);

  /** Row being edited (stock qty input) and its draft value. */
  editId: string | null = null;
  editQty = 0;
  /** Two-step confirm: first tap arms the row, second tap removes it. */
  confirmRemoveId: string | null = null;

  readonly addForm = this.fb.nonNullable.group({
    speciesId: ['', Validators.required],
    stockQty: [10, [Validators.required, Validators.min(1)]],
  });

  constructor() {
    this.refresh();
    this.api.treeSpecies().subscribe(s => this.species.set(s.filter(x => x.isActive)));
  }

  get rows(): SlotInventoryItem[] { return this.slot().inventory ?? []; }
  get treesAssigned(): number { return this.rows.reduce((n, r) => n + r.stockQty, 0); }
  get capacityLeft(): number { return Math.max(0, (this.slot().capacity ?? 0) - this.treesAssigned); }
  /** Species that are not on this slot yet. */
  get availableSpecies(): TreeSpecies[] {
    const used = new Set(this.rows.map(r => r.speciesId));
    return this.species().filter(s => !used.has(s.speciesId));
  }

  refresh(): void {
    this.api.slotDetail(this.slot().slotId).subscribe({
      next: s => this.slot.set(s),
      error: () => { /* keep the copy we already have */ },
    });
  }

  add(): void {
    if (this.addForm.invalid) { this.addForm.markAllAsTouched(); return; }
    const v = this.addForm.getRawValue();
    this.run(
      this.api.addInventory({ slotId: this.slot().slotId, speciesId: v.speciesId, stockQty: v.stockQty }),
      'Tree added to slot',
      () => this.addForm.reset({ speciesId: '', stockQty: 10 }),
    );
  }

  startEdit(row: SlotInventoryItem): void {
    this.confirmRemoveId = null;
    this.editId = row.inventoryId;
    this.editQty = row.stockQty;
  }

  saveEdit(row: SlotInventoryItem): void {
    this.run(this.api.updateInventory(row.inventoryId, this.editQty), 'Stock updated',
      () => { this.editId = null; });
  }

  requestRemove(row: SlotInventoryItem): void {
    if (this.confirmRemoveId !== row.inventoryId) { this.editId = null; this.confirmRemoveId = row.inventoryId; return; }
    this.confirmRemoveId = null;
    this.run(this.api.deleteInventory(row.inventoryId), 'Tree removed from slot');
  }

  cancelEdit(): void { this.editId = null; this.confirmRemoveId = null; }

  close(): void { this.ref.close(true); }

  private run(call: Observable<unknown>, done: string, after?: () => void): void {
    this.busy.set(true);
    call.subscribe({
      next: () => {
        this.busy.set(false);
        this.snack.open(done, 'OK', { duration: 2000 });
        after?.();
        this.refresh();
      },
      error: (e) => {
        this.busy.set(false);
        this.snack.open(e?.error?.message ?? 'Failed', 'Close', { duration: 5000 });
      },
    });
  }
}
