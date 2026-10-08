import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/http/api.service';
import { Occasion } from '../../../core/models';

@Component({
  selector: 'app-occasions',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule, MatTableModule, MatCardModule,
    MatButtonModule, MatIconModule, MatFormFieldModule, MatInputModule, MatSnackBarModule,
  ],
  templateUrl: './occasions.component.html',
  styleUrl: './occasions.component.css',
})
export class OccasionsComponent {
  private readonly api = inject(ApiService);
  private readonly snack = inject(MatSnackBar);
  private readonly fb = inject(FormBuilder);

  readonly occasions = signal<Occasion[]>([]);
  readonly loading = signal(true);
  readonly cols = ['emoji', 'name', 'order', 'status', 'actions'];
  readonly showForm = signal(false);
  editing: Occasion | null = null;

  readonly form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    emojiCode: ['🎉', Validators.required],
    description: [''],
    displayOrder: [0, Validators.required],
    isActive: [true],
  });

  constructor() { this.load(); }

  load(): void {
    this.api.occasions().subscribe({
      next: (o) => { this.occasions.set(o); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  openNew(): void {
    this.editing = null;
    this.form.reset({ name: '', emojiCode: '🎉', description: '', displayOrder: 0, isActive: true });
    this.showForm.set(true);
  }

  openEdit(o: Occasion): void {
    this.editing = o;
    this.form.setValue({ name: o.name, emojiCode: o.emojiCode, description: o.description ?? '', displayOrder: o.displayOrder, isActive: o.isActive });
    this.showForm.set(true);
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const body = this.form.getRawValue();
    const call = this.editing ? this.api.updateOccasion(this.editing.occasionId, body) : this.api.createOccasion(body);
    call.subscribe({
      next: () => { this.snack.open('Saved', 'OK', { duration: 2000 }); this.showForm.set(false); this.load(); },
      error: (e) => this.snack.open(e?.error?.message ?? 'Failed', 'Close', { duration: 3500 }),
    });
  }

  close(): void { this.showForm.set(false); this.editing = null; }
}
