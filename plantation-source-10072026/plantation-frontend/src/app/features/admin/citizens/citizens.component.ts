import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { ApiService } from '../../../core/http/api.service';
import { CitizenUser } from '../../../core/models';

@Component({
  selector: 'app-citizens',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatCardModule],
  templateUrl: './citizens.component.html',
  styleUrl: './citizens.component.css',
})
export class CitizensComponent {
  private readonly api = inject(ApiService);
  readonly citizens = signal<CitizenUser[]>([]);
  readonly loading = signal(true);
  readonly cols = ['name', 'email', 'phone', 'registered', 'status'];

  constructor() {
    this.api.citizens().subscribe({
      next: (c) => { this.citizens.set(c); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
}
