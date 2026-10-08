import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../../core/http/api.service';
import { CompletedPlantation } from '../../../core/models';

@Component({
  selector: 'app-completed',
  standalone: true,
  imports: [CommonModule, MatTableModule, MatCardModule, MatButtonModule],
  templateUrl: './completed.component.html',
  styleUrl: './completed.component.css',
})
export class CompletedComponent {
  private readonly api = inject(ApiService);
  readonly rows = signal<CompletedPlantation[]>([]);
  readonly loading = signal(true);
  readonly cols = ['ref', 'citizen', 'park', 'trees', 'date', 'cert'];

  constructor() {
    this.api.completedPlantations().subscribe({
      next: (r) => { this.rows.set(r); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
}
