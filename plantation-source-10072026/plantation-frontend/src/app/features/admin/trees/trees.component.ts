import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/http/api.service';
import { TreeSpecies, TreeCategory } from '../../../core/models';

@Component({
  selector: 'app-trees',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule, MatTableModule, MatCardModule,
    MatButtonModule, MatIconModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatSnackBarModule,
  ],
  templateUrl: './trees.component.html',
  styleUrl: './trees.component.css',
})
export class TreesComponent {
  private readonly api = inject(ApiService);
  private readonly snack = inject(MatSnackBar);
  private readonly dialog = inject(MatDialog);
  private readonly fb = inject(FormBuilder);

  readonly trees = signal<TreeSpecies[]>([]);
  readonly loading = signal(true);
  readonly cols = ['emoji', 'name', 'category', 'price', 'status', 'actions'];
  readonly categories: TreeCategory[] = ['Medicinal', 'Fruit', 'Sacred', 'Ornamental', 'Shade', 'Bamboo'];

  editing: TreeSpecies | null = null;
  readonly showForm = signal(false);

  readonly form = this.fb.nonNullable.group({
    commonName: ['', Validators.required],
    scientificName: [''],
    emojiCode: ['🌳'],
    price: [0, [Validators.required, Validators.min(0.01)]],
    category: ['Shade' as TreeCategory, Validators.required],
    benefits: [''],
    careNotes: [''],
    isActive: [true],
  });

  constructor() { this.load(); }

  load(): void {
    this.api.treeSpeciesAll().subscribe({
      next: (t) => { this.trees.set(t); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  openNew(): void {
    this.editing = null;
    this.form.reset({ commonName: '', scientificName: '', emojiCode: '🌳', price: 0, category: 'Shade', benefits: '', careNotes: '', isActive: true });
    this.showForm.set(true);
  }

  openEdit(t: TreeSpecies): void {
    this.editing = t;
    this.form.setValue({
      commonName: t.commonName, scientificName: t.scientificName ?? '',
      emojiCode: t.emojiCode ?? '🌳', price: t.price, category: t.category,
      benefits: t.benefits ?? '', careNotes: t.careNotes ?? '', isActive: t.isActive,
    });
    this.showForm.set(true);
  }

  save(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    const body = this.form.getRawValue();
    const call = this.editing ? this.api.updateTree(this.editing.speciesId, body) : this.api.createTree(body);
    call.subscribe({
      next: () => { this.snack.open('Saved', 'OK', { duration: 2000 }); this.showForm.set(false); this.load(); },
      error: (e) => this.snack.open(e?.error?.message ?? 'Failed', 'Close', { duration: 3500 }),
    });
  }

  deactivate(t: TreeSpecies): void {
    if (!confirm(`Deactivate "${t.commonName}"?`)) return;
    this.api.deactivateTree(t.speciesId).subscribe({
      next: () => { this.snack.open('Deactivated', 'OK', { duration: 2000 }); this.load(); },
      error: (e) => this.snack.open(e?.error?.message ?? 'Failed', 'Close', { duration: 3500 }),
    });
  }

  close(): void { this.showForm.set(false); this.editing = null; }
}
