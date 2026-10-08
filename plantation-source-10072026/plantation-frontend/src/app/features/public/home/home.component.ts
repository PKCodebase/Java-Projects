import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ApiService } from '../../../core/http/api.service';
import { Park } from '../../../core/models';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
})
export class HomeComponent {
  private readonly api = inject(ApiService);
  readonly parks = signal<Park[]>([]);
  readonly loading = signal(true);

  readonly features = [
    { icon: 'park', title: 'Adopt a Tree', text: 'Book a slot at your favourite park and plant a sapling.' },
    { icon: 'workspace_premium', title: 'Digital Certificate', text: 'Get a verifiable certificate for every tree you plant.' },
    { icon: 'event', title: 'Flexible Slots', text: 'Choose a date and time that suits you.' },
    { icon: 'eco', title: 'Green Delhi', text: 'Be part of a cleaner, greener city.' },
  ];

  constructor() {
    this.api.parksFlat().subscribe({
      next: (p) => { this.parks.set(p.slice(0, 3)); this.loading.set(false); },
      error: () => this.loading.set(false),
    });
  }
}
