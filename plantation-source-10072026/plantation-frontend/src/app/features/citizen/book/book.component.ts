import { ChangeDetectorRef, Component, ViewChild, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { MatStepper, MatStepperModule } from '@angular/material/stepper';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule, MatSelectionListChange } from '@angular/material/list';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatDividerModule } from '@angular/material/divider';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ApiService } from '../../../core/http/api.service';
import {
  Booking, Occasion, Park, ParkSlot, SlotInventoryItem, Zone,
} from '../../../core/models';
import { startOfToday, toLocalDate } from '../../../core/util/date.util';

/**
 * Citizen booking wizard — OCCASION → PARK → DATE & SLOT → TREE → CONFIRM →
 * PAYMENT, mirroring the MCD "Book a plantation slot (6 easy steps)" flow.
 *
 * Built entirely from Angular Material components: this component deliberately
 * ships **no stylesheet** (no `styleUrl`), so nothing here can drift away from
 * the app theme. Layout uses the existing global scaffolding only
 * (`.page`, `.grid*`, `.mt-16`, `.mb-16`, `.full-width`, `.toolbar-spacer`).
 *
 * Data:
 *  - Occasions come from `GET /occasions`; picking one loads
 *    `GET /occasions/{id}/species` so suitable species are marked
 *    "Recommended" in the TREE step (server-side mapping, not client logic).
 *  - `GET /parks/{id}/slots?date=` returns each slot **with its own
 *    inventory[]** — that is what the DATE & SLOT step previews per slot and
 *    what the TREE step sells from. Stock is per slot, so availability varies
 *    by slot exactly like the reference flow.
 *
 * Money: the confirm step shows the trees subtotal only. GST and the
 * processing fee are added by the server when the booking row is created, and
 * the payment step shows the authoritative `paymentOrder` breakdown it returns
 * — so the UI never repeats backend rates that could go stale.
 */
@Component({
  selector: 'app-book',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterLink,
    MatStepperModule, MatToolbarModule, MatCardModule, MatButtonModule, MatIconModule,
    MatListModule, MatChipsModule, MatFormFieldModule, MatInputModule,
    MatDatepickerModule, MatDividerModule, MatProgressBarModule,
  ],
  templateUrl: './book.component.html',
})
export class BookComponent {
  private readonly api = inject(ApiService);
  private readonly snack = inject(MatSnackBar);
  private readonly cdr = inject(ChangeDetectorRef);

  @ViewChild('stepper') stepper?: MatStepper;

  readonly today = startOfToday();

  // ── masters ────────────────────────────────────────────────────────
  readonly occasions = signal<Occasion[]>([]);
  readonly zones = signal<Zone[]>([]);
  readonly parks = signal<Park[]>([]);
  readonly slots = signal<ParkSlot[]>([]);
  readonly loadingMasters = signal(true);
  readonly loadingSlots = signal(false);

  // ── selection ──────────────────────────────────────────────────────
  readonly occasion = signal<Occasion | null>(null);
  readonly park = signal<Park | null>(null);
  readonly slot = signal<ParkSlot | null>(null);
  readonly quantities = signal<Record<string, number>>({});
  readonly date = signal<Date>(startOfToday());
  readonly parkQuery = signal('');
  readonly zoneFilter = signal('');

  /** speciesIds recommended for the chosen occasion (server mapping). */
  readonly recommendedIds = signal<ReadonlySet<string>>(new Set<string>());

  // ── booking + payment ──────────────────────────────────────────────
  readonly booking = signal<Booking | null>(null);
  readonly creating = signal(false);
  readonly paying = signal(false);
  readonly createError = signal('');
  readonly payMessage = signal('');

  constructor() {
    forkJoin({
      occasions: this.api.occasions(),
      zones: this.api.zones(),
      parks: this.api.parksFlat(),
    }).subscribe({
      next: ({ occasions, zones, parks }) => {
        this.occasions.set(occasions);
        this.zones.set(zones);
        this.parks.set(parks);
        this.loadingMasters.set(false);
      },
      error: (e) => {
        this.loadingMasters.set(false);
        this.snack.open(e?.error?.message ?? 'Could not load booking masters', 'Close',
          { duration: 4000 });
      },
    });
  }

  // ── step 1 · occasion ──────────────────────────────────────────────
  /**
   * Select an occasion (or null for "Any occasion") and ask the server which
   * species suit it — those get marked "Recommended" in the TREE step.
   */
  selectOccasion(o: Occasion | null): void {
    const current = this.occasion();
    if (o === null) {
      if (current === null) return;
      this.occasion.set(null);
      this.recommendedIds.set(new Set<string>());
      return;
    }
    if (current?.occasionId === o.occasionId) return;
    this.occasion.set(o);
    this.recommendedIds.set(new Set<string>());
    this.api.occasionSpecies(o.occasionId).subscribe({
      next: (species) =>
        this.recommendedIds.set(new Set(species.map(s => s.speciesId))),
      // No mapping configured — the step simply shows no "Recommended" marks.
      error: () => this.recommendedIds.set(new Set<string>()),
    });
  }

  isRecommended(speciesId: string): boolean {
    return this.recommendedIds().has(speciesId);
  }

  /**
   * Occasion only while the server actually returned species for it **and**
   * the chosen slot stocks at least one of them — otherwise the "look for
   * Recommended" hint would promise marks that can never appear.
   */
  readonly recommendationHint = computed(() => {
    const o = this.occasion();
    const rec = this.recommendedIds();
    const slot = this.slot();
    if (!o || rec.size === 0 || !slot) return null;
    const stocked = (slot.inventory ?? []).some(i => rec.has(i.speciesId));
    return stocked ? o : null;
  });

  /** Occasion with recommendations, but none of them stocked in this slot. */
  readonly recommendationMiss = computed(() => {
    const o = this.occasion();
    const rec = this.recommendedIds();
    const slot = this.slot();
    if (!o || rec.size === 0 || !slot) return null;
    const stocked = (slot.inventory ?? []).some(i => rec.has(i.speciesId));
    return stocked ? null : o;
  });

  // ── step 2 · park ──────────────────────────────────────────────────
  readonly filteredParks = computed(() => {
    const q = this.parkQuery().trim().toLowerCase();
    const zoneId = this.zoneFilter();
    return this.parks().filter(p => {
      const matchesZone = !zoneId || p.zone?.zoneId === zoneId;
      const matchesQuery = !q
        || (p.name ?? '').toLowerCase().includes(q)
        || (p.zone?.name ?? '').toLowerCase().includes(q)
        || (p.address ?? '').toLowerCase().includes(q);
      return matchesZone && matchesQuery;
    });
  });

  selectPark(p: Park): void {
    if (p.parkId === this.park()?.parkId) return;
    this.park.set(p);
    this.booking.set(null);           // selection changed → a new booking
    this.slot.set(null);
    this.quantities.set({});
    this.loadSlots();
  }

  // ── step 3 · date & slot ───────────────────────────────────────────
  onDate(value: Date | null): void {
    if (!value) return;
    this.date.set(value);
    this.slot.set(null);
    this.quantities.set({});
    this.loadSlots();
  }

  private loadSlots(): void {
    const park = this.park();
    if (!park) { this.slots.set([]); return; }
    this.loadingSlots.set(true);
    this.api.parkSlots(park.parkId, toLocalDate(this.date())).subscribe({
      next: (s) => { this.slots.set(s); this.loadingSlots.set(false); },
      error: () => {
        this.slots.set([]);
        this.loadingSlots.set(false);
        this.snack.open('Could not load slots for this date', 'Close', { duration: 3500 });
      },
    });
  }

  /**
   * Slot picked: its `inventory[]` (embedded in the slots response) becomes
   * both the right-hand "trees for this slot" preview and the TREE step's
   * stock. Quantities always restart at 0 when the slot changes.
   */
  onSlot(ev: MatSelectionListChange): void {
    const value = ev.options[0]?.value as ParkSlot | undefined;
    if (!value || value.slotId === this.slot()?.slotId) return;
    this.slot.set(value);
    this.booking.set(null);
    const quantities: Record<string, number> = {};
    (value.inventory ?? []).forEach(i => quantities[i.speciesId] = 0);
    this.quantities.set(quantities);
  }

  bookable(s: ParkSlot): boolean {
    return s.status === 'AVAILABLE' && s.freeSpots > 0;
  }

  // ── step 4 · trees ─────────────────────────────────────────────────
  count(speciesId: string): number {
    return this.quantities()[speciesId] ?? 0;
  }

  inc(item: SlotInventoryItem): void {
    const current = this.count(item.speciesId);
    if (current >= item.availableQty) return;
    this.booking.set(null);
    this.quantities.update(q => ({ ...q, [item.speciesId]: current + 1 }));
  }

  dec(item: SlotInventoryItem): void {
    const current = this.count(item.speciesId);
    if (current === 0) return;
    this.booking.set(null);
    this.quantities.update(q => ({ ...q, [item.speciesId]: current - 1 }));
  }

  readonly totalItems = computed(() =>
    Object.values(this.quantities()).reduce((sum, q) => sum + q, 0));

  readonly selectedRows = computed(() => {
    const slot = this.slot();
    if (!slot) return [];
    return (slot.inventory ?? [])
      .map(it => ({
        speciesId: it.speciesId,
        emoji: it.emojiCode || '🌳',
        name: it.commonName,
        qty: this.count(it.speciesId),
        unitPrice: it.price,
        lineTotal: it.price * this.count(it.speciesId),
      }))
      .filter(r => r.qty > 0);
  });

  readonly subtotal = computed(() =>
    this.selectedRows().reduce((sum, r) => sum + r.lineTotal, 0));

  // ── step 5 · confirm ───────────────────────────────────────────────

  /**
   * Advance to the payment step. The step's `[completed]` binding only reaches
   * `CdkStep` during change detection, so `stepper.next()` has to run *after*
   * that flush — otherwise the linear stepper silently refuses to move.
   */
  private goToPayment(): void {
    this.cdr.detectChanges();
    this.stepper?.next();
  }

  createBooking(): void {
    const slot = this.slot();
    if (!slot || this.totalItems() === 0) return;
    if (this.booking()) { this.goToPayment(); return; }

    const items = Object.entries(this.quantities())
      .filter(([, quantity]) => quantity > 0)
      .map(([speciesId, quantity]) => ({ speciesId, quantity }));

    const body: Record<string, unknown> = {
      slotId: slot.slotId,
      bookingDate: toLocalDate(this.date()),
      items,
    };
    const occasion = this.occasion();
    if (occasion) body['occasionId'] = occasion.occasionId;

    this.createError.set('');
    this.creating.set(true);
    this.api.createBooking(body).subscribe({
      next: (booking) => {
        this.creating.set(false);
        this.booking.set(booking);
        this.goToPayment();
      },
      error: (err) => {
        this.creating.set(false);
        this.createError.set(err?.error?.message ?? 'Could not create the booking. Please try again.');
      },
    });
  }

  // ── step 6 · payment ───────────────────────────────────────────────
  readonly paymentRows = computed(() => {
    const order = this.booking()?.paymentOrder;
    if (!order) return [];
    return [
      { label: 'Trees subtotal', amount: order.amount },
      { label: 'GST', amount: order.gstAmount },
      { label: 'Processing fee', amount: order.feeAmount },
      { label: 'Total payable', amount: order.totalAmount },
    ].filter(r => typeof r.amount === 'number') as { label: string; amount: number }[];
  });

  pay(): void {
    const booking = this.booking();
    if (!booking) return;

    this.payMessage.set('');
    this.paying.set(true);
    this.api.initPayment(booking.bookingId).subscribe({
      next: (res) => {
        this.paying.set(false);
        if (res?.url) {
          // Real gateway hand-off — the citizen leaves the SPA and comes back
          // on /citizen/payment/result once the gateway is done.
          window.location.assign(res.url);
        } else {
          this.payMessage.set(res?.message
            ?? 'The payment gateway did not return a payment URL. Please try again later.');
        }
      },
      error: (err) => {
        this.paying.set(false);
        this.payMessage.set(err?.error?.message ?? 'Could not start the payment. Please try again.');
      },
    });
  }
}
