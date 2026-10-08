import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/http/api.service';
import { Assignment, Official, Park } from '../../../core/models';

@Component({
  selector: 'app-assignments',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatTableModule, MatCardModule, MatButtonModule,
    MatIconModule, MatFormFieldModule, MatSelectModule, MatSnackBarModule,
  ],
  templateUrl: './assignments.component.html',
  styleUrl: './assignments.component.css',
})
export class AssignmentsComponent {
  private readonly api = inject(ApiService);
  private readonly snack = inject(MatSnackBar);

  readonly assignments = signal<Assignment[]>([]);
  readonly parks = signal<Park[]>([]);
  readonly officials = signal<Official[]>([]);
  readonly loading = signal(true);
  readonly cols = ['park', 'official', 'role', 'assignedAt', 'actions'];

  selectedPark = '';
  selectedOfficial = '';

  constructor() {
    this.api.assignments().subscribe({
      next: (a) => { this.assignments.set(a); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
    this.api.parksFlat().subscribe(p => this.parks.set(p));
    this.api.officials().subscribe(o => this.officials.set(o));
  }

  assign(): void {
    if (!this.selectedPark || !this.selectedOfficial) return;
    this.api.assignOfficial(this.selectedPark, this.selectedOfficial).subscribe({
      next: () => { this.snack.open('Official assigned', 'OK', { duration: 2500 }); this.reload(); },
      error: (e) => this.snack.open(e?.error?.message ?? 'Failed', 'Close', { duration: 3500 }),
    });
  }

  unassign(a: Assignment): void {
    if (!confirm('Remove this assignment?')) return;
    this.api.unassignOfficial(a.parkId, a.officialId).subscribe({
      next: () => { this.snack.open('Removed', 'OK', { duration: 2000 }); this.reload(); },
      error: (e) => this.snack.open(e?.error?.message ?? 'Failed', 'Close', { duration: 3500 }),
    });
  }

  private reload(): void {
    this.api.assignments().subscribe(a => this.assignments.set(a));
  }
}
