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
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/http/api.service';
import { Park, Zone } from '../../../core/models';
import { ParksDialogComponent, ParksDialogData } from './parks-dialog.component';

@Component({
  selector: 'app-parks',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule, MatTableModule, MatCardModule,
    MatButtonModule, MatIconModule, MatDialogModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatPaginatorModule, MatSnackBarModule,
  ],
  templateUrl: './parks.component.html',
  styleUrl: './parks.component.css',
})
export class ParksComponent {
  private readonly api = inject(ApiService);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);

  readonly parks = signal<Park[]>([]);
  readonly zones = signal<Zone[]>([]);
  readonly total = signal(0);
  readonly loading = signal(true);
  readonly displayed = ['name', 'zone', 'city', 'slots', 'status', 'actions'];

  pageSize = 9;
  pageIndex = 0;
  zoneFilter = '';

  constructor() {
    this.api.zones().subscribe(z => this.zones.set(z));
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.api.adminParks(this.zoneFilter || undefined, undefined, this.pageIndex, this.pageSize)
      .subscribe({
        next: (r) => { this.parks.set(r.content ?? []); this.total.set(r.totalElements ?? 0); this.loading.set(false); },
        error: () => this.loading.set(false),
      });
  }

  onPage(e: PageEvent) { this.pageIndex = e.pageIndex; this.load(); }

  openDialog(park?: Park): void {
    const data: ParksDialogData = { park, zones: this.zones() };
    this.dialog.open(ParksDialogComponent, { width: '520px', data })
      .afterClosed().subscribe(saved => { if (saved) this.load(); });
  }

  deactivate(park: Park): void {
    if (!confirm(`Deactivate "${park.name}"?`)) return;
    this.api.deletePark(park.parkId).subscribe({
      next: () => { this.snack.open('Park deactivated', 'OK', { duration: 2500 }); this.load(); },
      error: (e) => this.snack.open(e?.error?.message ?? 'Failed', 'Close', { duration: 3000 }),
    });
  }
}
