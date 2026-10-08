// ── Enums ──────────────────────────────────────────────────────────
export type BookingStatus =
  | 'PENDING_PAYMENT' | 'PAID' | 'SCHEDULED' | 'WAITING'
  | 'PLANTED' | 'COMPLETED' | 'CANCELLED';

export type OfficialRole = 'R_HORTIC_OFF' | 'SUPERVISOR' | 'R_HORTIC_ADM';
export type PaymentStatus = 'PENDING' | 'SUCCESS' | 'FAILED' | 'REFUNDED';
export type PresenceMode = 'PHYSICAL' | 'DELEGATED';
export type SlotStatus = 'AVAILABLE' | 'FULL' | 'CLOSED';
export type TreeCategory = 'Medicinal' | 'Fruit' | 'Sacred' | 'Ornamental' | 'Shade' | 'Bamboo';

// ── Auth ───────────────────────────────────────────────────────────
export interface UserSummary {
  id: string;
  fullName: string;
  email: string;
  role: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserSummary;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}

export interface LoginRequest { email: string; password: string; }

export interface CitizenRegisterRequest {
  fullName: string; email: string; password: string;
  phone?: string; aadhaarLast4?: string; address?: string;
}

/** Public Google sign-in configuration served by GET /auth/oauth2/google/config. */
export interface OauthConfig {
  enabled: boolean;
  clientId: string;
}

// ── Core masters ───────────────────────────────────────────────────
export interface Zone { zoneId: string; name: string; code: string; isActive: boolean; }

export interface Occasion {
  occasionId: string; name: string; description?: string;
  emojiCode: string; displayOrder: number; isActive: boolean;
}

export interface TreeSpecies {
  speciesId: string; commonName: string; scientificName?: string;
  emojiCode?: string; price: number; category: TreeCategory;
  benefits?: string; careNotes?: string; isActive: boolean;
  preferredOccasions?: Occasion[];
}

export interface Park {
  parkId: string; name: string; zone?: Zone; address?: string; city?: string;
  latitude?: number; longitude?: number; description?: string; isActive: boolean;
  totalSlots?: number; bookedSlots?: number; freeSlots?: number;
}

export interface SlotInventoryItem {
  inventoryId: string; speciesId: string; commonName: string;
  scientificName?: string; emojiCode?: string; category: TreeCategory;
  price: number; stockQty: number; reservedQty: number;
  availableQty: number; stockStatus: string;
}

export interface ParkSlot {
  slotId: string; parkId: string; parkName?: string; slotDate: string;
  startTime: string; endTime: string; capacity: number;
  bookedCount: number; freeSpots: number; status: SlotStatus;
  inventory?: SlotInventoryItem[];
}

// ── Bookings ───────────────────────────────────────────────────────
export interface BookingItem {
  inventoryId?: string; speciesId: string; treeName?: string;
  treeEmoji?: string; quantity: number; unitPrice?: number; totalPrice?: number;
}

export interface PaymentOrder {
  orderId?: string; currency?: string; amount?: number; gstAmount?: number;
  feeAmount?: number; totalAmount?: number; keyId?: string; url?: string;
}

export interface Booking {
  bookingId: string; bookingRef: string; citizenName?: string;
  citizenEmail?: string; citizenPhone?: string; citizenAadhaarLast4?: string;
  slotId: string; parkId: string; parkName?: string; slotDate?: string;
  slotTime?: string; items?: BookingItem[]; status: BookingStatus;
  presenceMode?: PresenceMode; amountPaid?: number; bookedAt?: string;
  paymentOrder?: PaymentOrder; occasionId?: string;
  occasionName?: string; occasionEmoji?: string;
}

// ── Certificates ───────────────────────────────────────────────────
export interface Certificate {
  certId: string; certNumber: string; citizenName?: string; parkName?: string;
  speciesId?: string; treeName?: string; treeEmoji?: string;
  scientificName?: string; quantity: number; treeIndex?: number;
  plantedDate?: string; slotTime?: string; presenceMode?: PresenceMode;
  photoUrl?: string; treeTagId?: string; pdfUrl?: string;
  isValid: boolean; issuedAt?: string;
}

// ── Dashboard ──────────────────────────────────────────────────────
export interface DashboardOverview {
  totalTreesPlanted: number; totalBookings: number; totalCertificatesIssued: number;
  bookingsPendingPayment: number; bookingsPaid: number; bookingsScheduled: number;
  bookingsWaiting: number; bookingsPlanted: number; bookingsCompleted: number;
  bookingsCancelled: number; treesPlantedToday: number; bookingsMadeToday: number;
  treesPlantedThisWeek: number; bookingsMadeThisWeek: number;
  treesPlantedThisMonth: number; bookingsMadeThisMonth: number;
  activeParkCount: number; upcomingSlotCount: number; lowStockAlertCount: number;
  recentActivity: RecentActivity[]; zoneBreakdown: ZoneStats[];
  speciesBreakdown: SpeciesStats[]; dailyTrend: DailyTrend[];
  topPendingParks: ParkPendingStats[]; generatedAt?: string;
}

export interface RecentActivity {
  bookingRef: string; citizenName: string; parkName: string;
  trees: { treeName: string; treeEmoji: string; quantity: number }[];
  officialName?: string; plantedDate?: string; recordedAt?: string;
}
export interface ZoneStats { zoneName: string; totalPlanted: number; scheduledBookings: number; totalBookings: number; }
export interface SpeciesStats { commonName: string; scientificName?: string; emojiCode?: string; totalPlanted: number; scheduled: number; }
export interface DailyTrend { date: string; planted: number; booked: number; }
export interface ParkPendingStats { parkName: string; zoneName?: string; pendingCount: number; }

// ── Officials / assignments ────────────────────────────────────────
export interface Official {
  officialId: string; name: string; employeeId?: string; email?: string;
  phone?: string; role: OfficialRole; zone?: Zone; isActive: boolean;
  createdAt?: string;
}

export interface Assignment {
  assignmentId: string; parkId: string; parkName?: string; parkZone?: Zone;
  officialId: string; officialName?: string; officialEmployeeId?: string;
  officialRole?: string; assignedAt?: string;
}

export interface CitizenUser {
  citizenId: string; fullName: string; email: string; phone?: string;
  aadhaarLast4?: string; address?: string; isActive: boolean;
  registeredAt?: string; lastLoginAt?: string;
}

// ── Inventory ──────────────────────────────────────────────────────
export interface MasterInventory {
  masterInvId: string; speciesId: string; commonName: string;
  scientificName?: string; emojiCode?: string; category: TreeCategory;
  price: number; totalQty: number; allocatedQty: number; availableQty: number;
}

export interface MasterInventoryHistory {
  historyId: string; action: string; speciesId: string; speciesName: string;
  emojiCode?: string; qty: number; parkId?: string; parkName?: string;
  performedBy?: string; performedAt?: string;
}

export interface ParkInventory {
  parkInvId: string; parkId: string; parkName?: string; speciesId: string;
  commonName: string; scientificName?: string; emojiCode?: string;
  category: TreeCategory; allocatedQty: number; usedQty: number;
  bookedQty: number; availableQty: number;
}

export interface LowStockAlert {
  inventoryId: string; slotId: string; slotDate?: string; slotTime?: string;
  parkId: string; parkName?: string; speciesId: string; treeName?: string;
  treeEmoji?: string; stockQty: number; reservedQty: number; availableQty: number;
}

export interface InventoryCell {
  inventoryId: string; speciesId: string; stockQty: number;
  reservedQty: number; availableQty: number; stockStatus: string;
}

export interface SlotMatrixRow {
  slotId: string; startTime: string; endTime: string; capacity: number;
  bookedCount: number; freeSpots: number; status: SlotStatus; cells: InventoryCell[];
}

export interface InventoryMatrix {
  parkId: string; parkName: string; date: string;
  speciesNames: string[]; rows: SlotMatrixRow[];
}

// ── Plantation records (officer) ───────────────────────────────────
export interface CompletedPlantation {
  recordId: string; bookingId: string; bookingRef: string; citizenName?: string;
  parkId?: string; parkName?: string;
  trees?: { speciesId: string; treeName: string; treeEmoji: string; quantity: number }[];
  slotDate?: string; slotTime?: string; plantedDate?: string;
  treeTagId?: string; photoUrl?: string; officialName?: string;
  certNumber?: string; pdfUrl?: string; certValid?: boolean; recordedAt?: string;
}

export interface PlantationRecord {
  recordId: string; bookingRef: string; citizenName?: string; parkName?: string;
  slotTime?: string;
  trees?: { speciesId: string; treeName: string; treeEmoji: string; quantity: number }[];
  treeTagId?: string; photoUrl?: string; gpsLat?: number; gpsLng?: number;
  plantedDate?: string; officialName?: string; recordedAt?: string;
}
