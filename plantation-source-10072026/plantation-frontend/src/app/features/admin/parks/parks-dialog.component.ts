import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/http/api.service';
import { Park, Zone } from '../../../core/models';

export interface ParksDialogData { park?: Park; zones: Zone[]; }

@Component({
  selector: 'app-parks-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatButtonModule, MatSnackBarModule,
  ],
  templateUrl: './parks-dialog.component.html',
  styleUrl: './parks-dialog.component.css',
})
export class ParksDialogComponent {
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ApiService);
  private readonly snack = inject(MatSnackBar);
  private readonly ref = inject(MatDialogRef<ParksDialogComponent>);
  readonly data = inject<ParksDialogData>(MAT_DIALOG_DATA);
  readonly saving = signal(false);

  readonly isEdit = !!this.data.park;

  readonly form = this.fb.nonNullable.group({
    name: [this.data.park?.name ?? '', Validators.required],
    zoneId: [this.data.park?.zone?.zoneId ?? '', Validators.required],
    address: [this.data.park?.address ?? ''],
    city: [this.data.park?.city ?? ''],
    latitude: [this.data.park?.latitude ?? null as number | null],
    longitude: [this.data.park?.longitude ?? null as number | null],
    description: [this.data.park?.description ?? ''],
    isActive: [this.data.park?.isActive ?? true],
  });

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const body = this.form.getRawValue();
    this.saving.set(true);
    const call = this.isEdit
      ? this.api.updatePark(this.data.park!.parkId, body)
      : this.api.createPark(body);

    call.subscribe({
      next: () => { this.saving.set(false); this.ref.close(true); },
      error: (e) => {
        this.saving.set(false);
        this.snack.open(e?.error?.message ?? 'Save failed', 'Close', { duration: 3500 });
      },
    });
  }
}
