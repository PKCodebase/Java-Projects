import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { ApiService } from '../../../core/http/api.service';
import { Park, ParkSlot } from '../../../core/models';
import { parseLocalDate, toLocalDate, todayLocalDate } from '../../../core/util/date.util';
import { SlotInventoryDialogComponent } from './slot-inventory-dialog.component';

@Component({
  selector: 'app-slots',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule, MatTableModule, MatCardModule,
    MatButtonModule, MatIconModule, MatFormFieldModule, MatSelectModule,
    MatInputModule, MatDatepickerModule, MatSnackBarModule, MatDialogModule,
  ],
  templateUrl: './slots.component.html',
  styleUrl: './slots.component.css',
})
export class SlotsComponent {
  private readonly api = inject(ApiService);
  private readonly snack = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);
  private readonly dialog = inject(MatDialog);

  readonly myParks = signal<Park[]>([]);
  readonly slots = signal<ParkSlot[]>([]);
  readonly loading = signal(true);
  readonly showForm = signal(false);
  readonly cols = ['time', 'date', 'trees', 'capacity', 'booked', 'status', 'actions'];

  selectedPark = '';
  editing: ParkSlot | null = null;
  /** Date used by the list filter picker. */
  dateFilter: Date = new Date();

  readonly form = this.fb.nonNullable.group({
    slotDate: [new Date() as Date | null, Validators.required],
    startTime: ['09:00', Validators.required],
    endTime: ['11:00', Validators.required],
    capacity: [20, [Validators.required, Validators.min(1)]],
  });

  constructor() {
    this.api.myParks().subscribe(p => {
      this.myParks.set(p);
      if (p.length) { this.selectedPark = p[0].parkId; this.load(); }
      else this.loading.set(false);
    });
  }

  load(): void {
    if (!this.selectedPark) return;
    this.loading.set(true);
    const d = this.dateFilter ? toLocalDate(this.dateFilter) : todayLocalDate();
    this.api.parkSlots(this.selectedPark, d).subscribe({
      next: (s) => { this.slots.set(s); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  openNew(): void {
    this.editing = null;
    this.form.reset({ slotDate: new Date(), startTime: '09:00', endTime: '11:00', capacity: 20 });
    this.showForm.set(true);
  }

  openEdit(s: ParkSlot): void {
    this.editing = s;
    this.form.setValue({
      slotDate: parseLocalDate(s.slotDate) ?? new Date(),
      startTime: s.startTime, endTime: s.endTime, capacity: s.capacity,
    });
    this.showForm.set(true);
  }

  save(): void {
    if (this.form.invalid || !this.selectedPark) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    const body = {
      parkId: this.selectedPark,
      slotDate: toLocalDate(v.slotDate),
      startTime: v.startTime, endTime: v.endTime, capacity: v.capacity,
    };
    const call = this.editing
      ? this.api.updateSlot(this.editing.slotId, body)
      : this.api.createSlot(body);
    call.subscribe({
      next: () => { this.snack.open('Slot saved', 'OK', { duration: 2000 }); this.showForm.set(false); this.load(); },
      error: (e) => this.snack.open(e?.error?.message ?? 'Failed', 'Close', { duration: 3500 }),
    });
  }

  closeSlot(s: ParkSlot): void {
    if (!confirm('Close this slot?')) return;
    this.api.closeSlot(s.slotId).subscribe({
      next: () => { this.snack.open('Slot closed', 'OK', { duration: 2000 }); this.load(); },
      error: (e) => this.snack.open(e?.error?.message ?? 'Failed', 'Close', { duration: 3500 }),
    });
  }

  /** Species assigned to a slot (drives the "Trees" column). */
  treeCount(s: ParkSlot): number { return (s.inventory ?? []).length; }

  /**
   * Opens the slot-inventory dialog where a species can be added to this slot,
   * its stock changed, or the species removed again.
   */
  openTrees(s: ParkSlot): void {
    this.dialog.open(SlotInventoryDialogComponent, {
      data: { slot: s },
      width: '660px',
      maxWidth: '95vw',
    }).afterClosed().subscribe(changed => { if (changed) this.load(); });
  }

  close(): void { this.showForm.set(false); this.editing = null; }
}
