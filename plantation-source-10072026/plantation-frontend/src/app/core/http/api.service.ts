import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable, OperatorFunction, map as rxMap } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ApiResponse, Assignment, Booking, Certificate, CompletedPlantation,
  DashboardOverview, InventoryMatrix, LowStockAlert, MasterInventory,
  MasterInventoryHistory, Occasion, Official, Park, ParkInventory, ParkSlot,
  PlantationRecord, TreeSpecies, Zone, CitizenUser,
} from '../models';

/**
 * Single typed gateway to the Spring REST API. Every method maps 1:1 to a
 * backend controller. All responses are unwrapped from ApiResponse<T>.data by
 * the `unwrap` helper so callers get clean domain objects.
 */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly base = environment.apiUrl;

  private unwrap<T>(res: ApiResponse<T>): T { return res?.data ?? (res as unknown as T); }

  // ── PUBLIC: masters ──────────────────────────────────────────────
  zones(): Observable<Zone[]> {
    return this.http.get<ApiResponse<Zone[]>>(`${this.base}/zones`)
      .pipe(this.map());
  }

  parks(zoneId?: string, page = 0, size = 9): Observable<{ content: Park[]; totalElements: number }> {
    let p = new HttpParams().set('page', page).set('size', size);
    if (zoneId) p = p.set('zoneId', zoneId);
    return this.http.get<ApiResponse<{ content: Park[]; totalElements: number }>>(`${this.base}/parks`, { params: p })
      .pipe(this.map());
  }

  parksFlat(zoneId?: string): Observable<Park[]> {
    let p = new HttpParams();
    if (zoneId) p = p.set('zoneId', zoneId);
    return this.http.get<ApiResponse<Park[]>>(`${this.base}/parks/all`, { params: p }).pipe(this.map());
  }

  park(id: string): Observable<Park> {
    return this.http.get<ApiResponse<Park>>(`${this.base}/parks/${id}`).pipe(this.map());
  }

  parkSlots(parkId: string, date: string): Observable<ParkSlot[]> {
    return this.http.get<ApiResponse<ParkSlot[]>>(`${this.base}/parks/${parkId}/slots`, { params: { date } })
      .pipe(this.map());
  }

  occasions(): Observable<Occasion[]> {
    return this.http.get<ApiResponse<Occasion[]>>(`${this.base}/occasions`).pipe(this.map());
  }

  occasionSpecies(occasionId: string): Observable<TreeSpecies[]> {
    return this.http.get<ApiResponse<TreeSpecies[]>>(`${this.base}/occasions/${occasionId}/species`)
      .pipe(this.map());
  }

  treeSpecies(): Observable<TreeSpecies[]> {
    return this.http.get<ApiResponse<TreeSpecies[]>>(`${this.base}/trees`).pipe(this.map());
  }

  /** Full catalog including inactive species — MCD staff only (GET /trees/all). */
  treeSpeciesAll(): Observable<TreeSpecies[]> {
    return this.http.get<ApiResponse<TreeSpecies[]>>(`${this.base}/trees/all`).pipe(this.map());
  }

  slotDetail(slotId: string): Observable<ParkSlot> {
    return this.http.get<ApiResponse<ParkSlot>>(`${this.base}/slots/${slotId}`).pipe(this.map());
  }

  // ── CERTIFICATES (public verify) ─────────────────────────────────
  certificatesForBooking(bookingId: string): Observable<Certificate[]> {
    return this.http.get<ApiResponse<Certificate[]>>(`${this.base}/certificates/${bookingId}`)
      .pipe(this.map());
  }

  verifyCertificate(certNumber: string): Observable<Certificate> {
    return this.http.get<ApiResponse<Certificate>>(`${this.base}/certificates/verify/${certNumber}`)
      .pipe(this.map());
  }

  // ── CITIZEN: bookings & payment ──────────────────────────────────
  createBooking(body: unknown): Observable<Booking> {
    return this.http.post<ApiResponse<Booking>>(`${this.base}/bookings`, body).pipe(this.map());
  }

  myBookings(): Observable<Booking[]> {
    return this.http.get<ApiResponse<Booking[]>>(`${this.base}/bookings/mine`).pipe(this.map());
  }

  booking(id: string): Observable<Booking> {
    return this.http.get<ApiResponse<Booking>>(`${this.base}/bookings/${id}`).pipe(this.map());
  }

  setPresence(bookingId: string, presenceMode: 'PHYSICAL' | 'DELEGATED'): Observable<Booking> {
    return this.http.put<ApiResponse<Booking>>(`${this.base}/bookings/${bookingId}/presence`,
      { presenceMode }).pipe(this.map());
  }

  cancelBooking(bookingId: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/bookings/${bookingId}`).pipe(this.map());
  }

  /**
   * POST /bookings/{id}/payment/init — hands back the redirect URL of the
   * payment session the gateway created with the booking (never a gateway
   * payload). `message` explains the no-URL case to the user.
   */
  initPayment(bookingId: string): Observable<{ url: string | null; message?: string }> {
    return this.http.post<ApiResponse<{ url: string | null; message?: string }>>(
      `${this.base}/bookings/${bookingId}/payment/init`, {})
      .pipe(this.map());
  }

  /** POST /bookings/payment/verify — `encryptedResponse` is the gateway's return payload. */
  verifyPayment(encryptedResponse: string): Observable<Booking> {
    return this.http.post<ApiResponse<Booking>>(
      `${this.base}/bookings/payment/verify`, null, { params: { encryptedResponse } })
      .pipe(this.map());
  }

  /**
   * TEST MODE ONLY (backend must run with PG_TEST_MODE=true):
   * POST /bookings/{id}/payment/test-payload — mints a clearly-labelled fake
   * gateway response that the REAL verify endpoint then consumes. The backend
   * answers 404 when test mode is off, so this can never work elsewhere.
   */
  testPaymentPayload(bookingId: string, result: 'success' | 'failure'): Observable<string> {
    return this.http.post<ApiResponse<{ encryptedResponse: string }>>(
      `${this.base}/bookings/${bookingId}/payment/test-payload`, null, { params: { result } })
      .pipe(this.map(), rxMap(res => res.encryptedResponse));
  }

  // ── ADMIN: dashboard ─────────────────────────────────────────────
  dashboard(): Observable<DashboardOverview> {
    return this.http.get<ApiResponse<DashboardOverview>>(`${this.base}/mcd/dashboard`).pipe(this.map());
  }

  // ── ADMIN: parks ─────────────────────────────────────────────────
  adminParks(zoneId?: string, name?: string, page = 0, size = 9) {
    let p = new HttpParams().set('page', page).set('size', size);
    if (zoneId) p = p.set('zoneId', zoneId);
    if (name) p = p.set('name', name);
    return this.http.get<ApiResponse<{ content: Park[]; totalElements: number }>>(
      `${this.base}/mcd/parks/paged`, { params: p }).pipe(this.map());
  }

  createPark(body: unknown): Observable<Park> {
    return this.http.post<ApiResponse<Park>>(`${this.base}/mcd/parks`, body).pipe(this.map());
  }

  updatePark(id: string, body: unknown): Observable<Park> {
    return this.http.put<ApiResponse<Park>>(`${this.base}/mcd/parks/${id}`, body).pipe(this.map());
  }

  deletePark(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/mcd/parks/${id}`).pipe(this.map());
  }

  // ── ADMIN: officials ─────────────────────────────────────────────
  officials(): Observable<Official[]> {
    return this.http.get<ApiResponse<Official[]>>(`${this.base}/mcd/officials`).pipe(this.map());
  }

  official(id: string): Observable<Official> {
    return this.http.get<ApiResponse<Official>>(`${this.base}/mcd/officials/${id}`).pipe(this.map());
  }

  zoneOfficials(zoneId: string): Observable<Official[]> {
    return this.http.get<ApiResponse<Official[]>>(`${this.base}/mcd/zones/${zoneId}/officials`)
      .pipe(this.map());
  }

  // ── ADMIN: assignments ───────────────────────────────────────────
  assignments(): Observable<Assignment[]> {
    return this.http.get<ApiResponse<Assignment[]>>(`${this.base}/mcd/assignments`).pipe(this.map());
  }

  assignmentsByPark(parkId: string): Observable<Assignment[]> {
    return this.http.get<ApiResponse<Assignment[]>>(`${this.base}/mcd/assignments/parks/${parkId}`)
      .pipe(this.map());
  }

  assignOfficial(parkId: string, officialId: string): Observable<Assignment> {
    return this.http.post<ApiResponse<Assignment>>(
      `${this.base}/mcd/assignments/parks/${parkId}/officials`, { officialId }).pipe(this.map());
  }

  unassignOfficial(parkId: string, officialId: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(
      `${this.base}/mcd/assignments/parks/${parkId}/officials/${officialId}`).pipe(this.map());
  }

  // ── ADMIN: tree species ──────────────────────────────────────────
  createTree(body: unknown): Observable<TreeSpecies> {
    return this.http.post<ApiResponse<TreeSpecies>>(`${this.base}/mcd/trees`, body).pipe(this.map());
  }

  updateTree(id: string, body: unknown): Observable<TreeSpecies> {
    return this.http.put<ApiResponse<TreeSpecies>>(`${this.base}/mcd/trees/${id}`, body).pipe(this.map());
  }

  deactivateTree(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/mcd/trees/${id}`).pipe(this.map());
  }

  updateTreeOccasions(id: string, occasionIds: string[]): Observable<TreeSpecies> {
    return this.http.put<ApiResponse<TreeSpecies>>(`${this.base}/mcd/trees/${id}/occasions`, occasionIds)
      .pipe(this.map());
  }

  // ── ADMIN: occasions ─────────────────────────────────────────────
  createOccasion(body: unknown): Observable<Occasion> {
    return this.http.post<ApiResponse<Occasion>>(`${this.base}/mcd/occasions`, body).pipe(this.map());
  }

  updateOccasion(id: string, body: unknown): Observable<Occasion> {
    return this.http.put<ApiResponse<Occasion>>(`${this.base}/mcd/occasions/${id}`, body).pipe(this.map());
  }

  // ── ADMIN: master inventory ──────────────────────────────────────
  masterInventory(): Observable<MasterInventory[]> {
    return this.http.get<ApiResponse<MasterInventory[]>>(`${this.base}/mcd/master-inventory`)
      .pipe(this.map());
  }

  setMasterStock(speciesId: string, qty: number): Observable<MasterInventory[]> {
    return this.http.put<ApiResponse<MasterInventory[]>>(`${this.base}/mcd/master-inventory`,
      { speciesId, qty }).pipe(this.map());
  }

  masterHistory(): Observable<MasterInventoryHistory[]> {
    return this.http.get<ApiResponse<MasterInventoryHistory[]>>(`${this.base}/mcd/master-inventory/history`)
      .pipe(this.map());
  }

  // ── ADMIN: citizens ──────────────────────────────────────────────
  citizens(): Observable<CitizenUser[]> {
    return this.http.get<ApiResponse<CitizenUser[]>>(`${this.base}/mcd/citizens`).pipe(this.map());
  }

  // ── OFFICER: my parks & slots ────────────────────────────────────
  myParks(): Observable<Park[]> {
    return this.http.get<ApiResponse<Park[]>>(`${this.base}/mcd/my-parks`).pipe(this.map());
  }

  createSlot(body: unknown): Observable<ParkSlot> {
    return this.http.post<ApiResponse<ParkSlot>>(`${this.base}/mcd/slots`, body).pipe(this.map());
  }

  updateSlot(slotId: string, body: unknown): Observable<ParkSlot> {
    return this.http.put<ApiResponse<ParkSlot>>(`${this.base}/mcd/slots/${slotId}`, body).pipe(this.map());
  }

  closeSlot(slotId: string): Observable<void> {
    return this.http.put<ApiResponse<void>>(`${this.base}/mcd/slots/${slotId}/close`, {}).pipe(this.map());
  }

  // ── OFFICER: plantation work ─────────────────────────────────────
  pendingPlantations(fromDate?: string, toDate?: string): Observable<Booking[]> {
    let p = new HttpParams();
    if (fromDate) p = p.set('fromDate', fromDate);
    if (toDate) p = p.set('toDate', toDate);
    return this.http.get<ApiResponse<Booking[]>>(`${this.base}/mcd/plantation/pending`, { params: p })
      .pipe(this.map());
  }

  pendingToday(): Observable<Booking[]> {
    return this.http.get<ApiResponse<Booking[]>>(`${this.base}/mcd/plantation/pendingToday`)
      .pipe(this.map());
  }

  completedPlantations(parkId?: string, fromDate?: string, toDate?: string) {
    let p = new HttpParams();
    if (parkId) p = p.set('parkId', parkId);
    if (fromDate) p = p.set('fromDate', fromDate);
    if (toDate) p = p.set('toDate', toDate);
    return this.http.get<ApiResponse<CompletedPlantation[]>>(`${this.base}/mcd/plantation/completed`, { params: p })
      .pipe(this.map());
  }

  markArrived(bookingId: string): Observable<Booking> {
    return this.http.patch<ApiResponse<Booking>>(`${this.base}/mcd/plantation/${bookingId}/arrive`, {})
      .pipe(this.map());
  }

  reschedule(bookingId: string, slotId: string): Observable<Booking> {
    return this.http.put<ApiResponse<Booking>>(`${this.base}/mcd/plantation/${bookingId}/reschedule`,
      { slotId }).pipe(this.map());
  }

  recordPlantation(bookingId: string, body: unknown): Observable<PlantationRecord> {
    return this.http.post<ApiResponse<PlantationRecord>>(`${this.base}/mcd/plantation/${bookingId}`, body)
      .pipe(this.map());
  }

  // ── OFFICER: inventory ───────────────────────────────────────────
  inventoryMatrix(parkId: string, date: string): Observable<InventoryMatrix> {
    return this.http.get<ApiResponse<InventoryMatrix>>(`${this.base}/mcd/inventory/matrix`,
      { params: { parkId, date } }).pipe(this.map());
  }

  lowStock(): Observable<LowStockAlert[]> {
    return this.http.get<ApiResponse<LowStockAlert[]>>(`${this.base}/mcd/inventory/low-stock`)
      .pipe(this.map());
  }

  addInventory(body: unknown): Observable<unknown> {
    return this.http.post<ApiResponse<unknown>>(`${this.base}/mcd/inventory`, body).pipe(this.map());
  }

  updateInventory(id: string, stockQty: number): Observable<unknown> {
    return this.http.put<ApiResponse<unknown>>(`${this.base}/mcd/inventory/${id}`, { stockQty })
      .pipe(this.map());
  }

  deleteInventory(id: string): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.base}/mcd/inventory/${id}`).pipe(this.map());
  }

  parkInventory(parkId: string): Observable<ParkInventory[]> {
    return this.http.get<ApiResponse<ParkInventory[]>>(`${this.base}/mcd/parks/${parkId}/inventory`)
      .pipe(this.map());
  }

  allocateToPark(parkId: string, speciesId: string, qty: number): Observable<ParkInventory[]> {
    return this.http.post<ApiResponse<ParkInventory[]>>(`${this.base}/mcd/parks/${parkId}/inventory`,
      { speciesId, qty }).pipe(this.map());
  }

  // rxjs map helper that unwraps ApiResponse<T>.data → T
  private map(): OperatorFunction<ApiResponse<any>, any> {
    return rxMap((res: ApiResponse<any>) => this.unwrap(res));
  }
}
