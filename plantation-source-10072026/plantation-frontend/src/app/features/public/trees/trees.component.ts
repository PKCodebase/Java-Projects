import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../../../core/http/api.service';
import { TreeSpecies } from '../../../core/models';

@Component({
  selector: 'app-trees',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatCardModule, MatFormFieldModule,
    MatInputModule, MatChipsModule, MatSelectModule, MatButtonModule,
  ],
  templateUrl: './trees.component.html',
  styleUrl: './trees.component.css',
})
export class TreesComponent {
  private readonly api = inject(ApiService);
  readonly all = signal<TreeSpecies[]>([]);
  readonly loading = signal(true);
  readonly error = signal<string | null>(null);
  search = '';
  category = '';

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);
    this.api.treeSpecies().subscribe({
      next: (t) => { this.all.set(t); this.loading.set(false); },
      error: (e) => {
        this.error.set(e?.error?.message ?? 'Could not load the tree species. Please try again.');
        this.loading.set(false);
      },
    });
  }

  /** Search handler: also drops a category that no longer has any match. */
  onSearch(value: string): void {
    this.search = value;
    if (this.category && !this.categories.includes(this.category)) this.category = '';
  }

  onCategory(value: string): void {
    this.category = value;
  }

  /**
   * Categories offered in the dropdown are the ones that actually occur in the
   * current search results, so every category you can pick leads to at least
   * one card — no dead ends after searching.
   */
  get categories(): string[] {
    return [...new Set(this.searchMatches.map(t => t.category))].sort();
  }

  get filtered(): TreeSpecies[] {
    const list = this.searchMatches;
    return this.category ? list.filter(t => t.category === this.category) : list;
  }

  /** Search text applied first; the category filter narrows that result. */
  private get searchMatches(): TreeSpecies[] {
    const q = this.search.trim().toLowerCase();
    if (!q) return this.all();
    return this.all().filter(t =>
      t.commonName.toLowerCase().includes(q) ||
      (t.scientificName ?? '').toLowerCase().includes(q));
  }
}
