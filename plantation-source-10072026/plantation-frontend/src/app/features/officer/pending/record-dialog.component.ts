import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/http/api.service';
import { Booking } from '../../../core/models';
import { todayLocalDate, toLocalDate } from '../../../core/util/date.util';

export interface RecordDialogData { booking: Booking; }

@Component({
  selector: 'app-record-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatDatepickerModule, MatButtonModule, MatSnackBarModule,
  ],
  templateUrl: './record-dialog.component.html',
  styleUrl: './record-dialog.component.css',
})
export class RecordDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ApiService);
  private readonly snack = inject(MatSnackBar);
  private readonly ref = inject(MatDialogRef<RecordDialogComponent>);
  readonly data = inject<RecordDialogData>(MAT_DIALOG_DATA);
  readonly saving = signal(false);

  readonly form = this.fb.nonNullable.group({
    treeTagId: ['', Validators.required],
    photoUrl: [''],
    gpsLat: [null as number | null],
    gpsLng: [null as number | null],
    plantedDate: [new Date() as Date | null, Validators.required],
    notes: [''],
  });

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const v = this.form.getRawValue();
    const body: Record<string, unknown> = {
      treeTagId: v.treeTagId,
      photoUrl: v.photoUrl || undefined,
      gpsLat: v.gpsLat,
      gpsLng: v.gpsLng,
      notes: v.notes || undefined,
      plantedDate: v.plantedDate ? toLocalDate(v.plantedDate) : todayLocalDate(),
    };

    this.saving.set(true);
    this.api.recordPlantation(this.data.booking.bookingId, body).subscribe({
      next: () => {
        this.saving.set(false);
        this.snack.open('Plantation recorded', 'OK', { duration: 2200 });
        this.ref.close(true);
      },
      error: (e) => {
        this.saving.set(false);
        this.snack.open(e?.error?.message ?? 'Failed to record', 'Close', { duration: 3500 });
      },
    });
  }
}
