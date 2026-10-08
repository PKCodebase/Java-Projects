import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatSelectModule } from '@angular/material/select';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/http/api.service';
import { AuthService } from '../../../core/auth/auth.service';
import { Occasion, Park, ParkSlot } from '../../../core/models';
import { startOfToday, toLocalDate } from '../../../core/util/date.util';

@Component({
  selector: 'app-park-detail',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink, MatCardModule, MatButtonModule,
    MatIconModule, MatDatepickerModule, MatFormFieldModule, MatInputModule,
    MatChipsModule, MatSelectModule, MatDividerModule, MatSnackBarModule,
  ],
  templateUrl: './park-detail.component.html',
  styleUrl: './park-detail.component.css',
})
export class ParkDetailComponent {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);
  readonly auth = inject(AuthService);
  readonly today = startOfToday();

  readonly park = signal<Park | null>(null);
  readonly slots = signal<ParkSlot[]>([]);
  readonly occasions = signal<Occasion[]>([]);
  readonly loadingSlots = signal(false);
  readonly booking = signal(false);

  date = new Date();
  selectedSlot: ParkSlot | null = null;
  selectedOccasion = '';
  quantities: Record<string, number> = {};

  constructor() {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.api.park(id).subscribe(p => this.park.set(p));
    this.api.occasions().subscribe(o => this.occasions.set(o));
    this.loadSlots();
  }

  loadSlots(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    const d = toLocalDate(this.date);
    this.loadingSlots.set(true);
    this.selectedSlot = null;
    this.api.parkSlots(id, d).subscribe({
      next: (s) => { this.slots.set(s); this.loadingSlots.set(false); },
      error: () => this.loadingSlots.set(false),
    });
  }

  selectSlot(slot: ParkSlot): void {
    this.selectedSlot = slot;
    this.quantities = {};
    (slot.inventory ?? []).forEach(i => this.quantities[i.speciesId] = 0);
  }

  inc(item: { speciesId: string; availableQty: number }): void {
    const cur = this.quantities[item.speciesId] ?? 0;
    if (cur < item.availableQty) this.quantities[item.speciesId] = cur + 1;
  }
  dec(item: { speciesId: string }): void {
    const cur = this.quantities[item.speciesId] ?? 0;
    if (cur > 0) this.quantities[item.speciesId] = cur - 1;
  }
  count(id: string): number { return this.quantities[id] ?? 0; }

  totalItems(): number {
    return Object.values(this.quantities).reduce((a, b) => a + b, 0);
  }

  book(): void {
    if (!this.auth.isAuthenticated) {
      this.snack.open('Please sign in to book a slot', 'OK', { duration: 3000 });
      this.router.navigate(['/login']);
      return;
    }
    if (!this.selectedSlot || this.totalItems() === 0) return;

    const items = Object.entries(this.quantities)
      .filter(([, q]) => q > 0)
      .map(([speciesId, quantity]) => ({ speciesId, quantity }));

    const body: Record<string, unknown> = {
      slotId: this.selectedSlot.slotId,
      bookingDate: toLocalDate(this.date),
      items,
    };
    if (this.selectedOccasion) body['occasionId'] = this.selectedOccasion;

    this.booking.set(true);
    this.api.createBooking(body).subscribe({
      next: (b) => {
        this.booking.set(false);
        this.snack.open(`Booking ${b.bookingRef} created!`, 'OK', { duration: 3000 });
        this.router.navigate(['/citizen/bookings', b.bookingId]);
      },
      error: (err) => {
        this.booking.set(false);
        this.snack.open(err?.error?.message ?? 'Booking failed', 'Close', { duration: 4000 });
      },
    });
  }
}
