import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { ApiService } from '../../../core/http/api.service';
import { Official } from '../../../core/models';

@Component({
  selector: 'app-officials',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatCardModule, MatButtonModule, MatIconModule, MatChipsModule],
  templateUrl: './officials.component.html',
  styleUrl: './officials.component.css',
})
export class OfficialsComponent {
  private readonly api = inject(ApiService);
  readonly officials = signal<Official[]>([]);
  readonly loading = signal(true);
  readonly cols = ['name', 'employeeId', 'email', 'role', 'zone', 'status'];

  constructor() {
    this.api.officials().subscribe({
      next: (o) => { this.officials.set(o); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }

  roleLabel(r: string): string {
    return ({ R_HORTIC_OFF: 'Horticulture Officer', SUPERVISOR: 'Supervisor', R_HORTIC_ADM: 'Admin' } as Record<string, string>)[r] ?? r;
  }
}
