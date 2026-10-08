/**
 * Local calendar-date helpers.
 *
 * IMPORTANT — never build a `YYYY-MM-DD` API value with
 * `Date.prototype.toISOString()`. That converts to **UTC**, so on an IST
 * (+05:30) clock a date picked in the UI was sent as the *previous* day: the
 * park-detail page asked for slots one day before the chosen date (and the
 * officer slot form saved a slot one day early). These helpers keep the
 * calendar day the user actually sees.
 */

/** The given date's local calendar day as `YYYY-MM-DD`. */
export function toLocalDate(value: Date | null | undefined): string {
  const d = value ?? new Date();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${d.getFullYear()}-${month}-${day}`;
}

/** Today's local calendar day as `YYYY-MM-DD`. */
export function todayLocalDate(): string {
  return toLocalDate(new Date());
}

/** Local midnight of today — safe `min` for a datepicker. */
export function startOfToday(): Date {
  const d = new Date();
  d.setHours(0, 0, 0, 0);
  return d;
}

/**
 * Parses a `YYYY-MM-DD` API value as a **local** date. `new Date('YYYY-MM-DD')`
 * would parse it as UTC midnight, which can shift the day in a negative
 * timezone offset.
 */
export function parseLocalDate(value: string | null | undefined): Date | null {
  if (!value) return null;
  const [y, m, d] = value.split('-').map(Number);
  if (!y || !m || !d) return null;
  return new Date(y, m - 1, d);
}
