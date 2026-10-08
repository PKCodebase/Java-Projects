import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/http/api.service';
import { Booking } from '../../../core/models';
import { toLocalDate } from '../../../core/util/date.util';
import { RecordDialogComponent, RecordDialogData } from './record-dialog.component';

@Component({
  selector: 'app-pending',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule, MatCardModule, MatButtonModule,
    MatIconModule, MatTableModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatDatepickerModule, MatSnackBarModule,
  ],
  templateUrl: './pending.component.html',
  styleUrl: './pending.component.css',
})
export class PendingComponent {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);

  readonly rows = signal<Booking[]>([]);
  readonly todayRows = signal<Booking[]>([]);
  readonly loading = signal(true);
  readonly cols = ['ref', 'citizen', 'park', 'date', 'trees', 'actions'];

  fromDate: Date | null = null;
  toDate: Date | null = null;

  constructor() { this.load(); }

  load(): void {
    this.loading.set(true);
    const f = this.fromDate ? toLocalDate(this.fromDate) : undefined;
    const t = this.toDate ? toLocalDate(this.toDate) : undefined;
    this.api.pendingPlantations(f, t).subscribe({
      next: (b) => { this.rows.set(b); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
    this.api.pendingToday().subscribe(b => this.todayRows.set(b));
  }

  markArrived(b: Booking): void {
    this.api.markArrived(b.bookingId).subscribe({
      next: () => { this.snack.open('Marked arrived', 'OK', { duration: 2000 }); this.load(); },
      error: (e) => this.snack.open(e?.error?.message ?? 'Failed', 'Close', { duration: 3500 }),
    });
  }

  record(b: Booking): void {
    const data: RecordDialogData = { booking: b };
    this.dialog.open(RecordDialogComponent, { width: '520px', data })
      .afterClosed().subscribe(saved => { if (saved) this.load(); });
  }
}
