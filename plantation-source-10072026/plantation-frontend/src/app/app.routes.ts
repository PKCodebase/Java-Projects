import { Routes } from '@angular/router';
import { authGuard, roleGuard } from './core/auth/auth.guard';

export const routes: Routes = [
  // ── Public / auth ────────────────────────────────────────────────
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login.component')
      .then(m => m.LoginComponent),
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register.component')
      .then(m => m.RegisterComponent),
  },
  {
    path: 'forgot-password',
    loadComponent: () => import('./features/auth/forgot-password/forgot-password.component')
      .then(m => m.ForgotPasswordComponent),
  },
  {
    path: 'otp-login',
    loadComponent: () => import('./features/auth/otp-login/otp-login.component')
      .then(m => m.OtpLoginComponent),
  },

  // ── Public citizen site (browse parks, verify certificate) ───────
  {
    path: '',
    loadComponent: () => import('./features/public/public-layout/public-layout.component')
      .then(m => m.PublicLayoutComponent),
    children: [
      { path: 'home', canActivate: [], loadComponent: () => import('./features/public/home/home.component').then(m => m.HomeComponent) },
      { path: 'parks', loadComponent: () => import('./features/public/parks/parks.component').then(m => m.ParksComponent) },
      { path: 'parks/:id', loadComponent: () => import('./features/public/park-detail/park-detail.component').then(m => m.ParkDetailComponent) },
      { path: 'trees', loadComponent: () => import('./features/public/trees/trees.component').then(m => m.TreesComponent) },
      { path: 'verify', loadComponent: () => import('./features/public/verify/verify.component').then(m => m.VerifyComponent) },
    ],
  },

  // ── Citizen dashboard ────────────────────────────────────────────
  {
    path: 'citizen',
    canActivate: [roleGuard],
    data: { roles: ['CITIZEN'] },
    loadComponent: () => import('./features/citizen/citizen-layout/citizen-layout.component')
      .then(m => m.CitizenLayoutComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', loadComponent: () => import('./features/citizen/dashboard/dashboard.component').then(m => m.CitizenDashboardComponent) },
      { path: 'book', loadComponent: () => import('./features/citizen/book/book.component').then(m => m.BookComponent) },
      { path: 'payment/result', loadComponent: () => import('./features/citizen/payment-result/payment-result.component').then(m => m.PaymentResultComponent) },
      { path: 'payment/test', loadComponent: () => import('./features/citizen/payment-test/payment-test.component').then(m => m.PaymentTestComponent) },
      { path: 'bookings', loadComponent: () => import('./features/citizen/my-bookings/my-bookings.component').then(m => m.MyBookingsComponent) },
      { path: 'bookings/:id', loadComponent: () => import('./features/citizen/booking-detail/booking-detail.component').then(m => m.BookingDetailComponent) },
      { path: 'certificates', loadComponent: () => import('./features/citizen/certificates/certificates.component').then(m => m.CertificatesComponent) },
    ],
  },

  // ── Officer dashboard ────────────────────────────────────────────
  {
    path: 'officer',
    canActivate: [roleGuard],
    data: { roles: ['R_HORTIC_OFF', 'SUPERVISOR', 'R_HORTIC_ADM'] },
    loadComponent: () => import('./features/officer/officer-layout/officer-layout.component')
      .then(m => m.OfficerLayoutComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', loadComponent: () => import('./features/officer/odashboard/odashboard.component').then(m => m.ODashboardComponent) },
      { path: 'pending', loadComponent: () => import('./features/officer/pending/pending.component').then(m => m.PendingComponent) },
      { path: 'completed', loadComponent: () => import('./features/officer/completed/completed.component').then(m => m.CompletedComponent) },
      { path: 'slots', loadComponent: () => import('./features/officer/slots/slots.component').then(m => m.SlotsComponent) },
      { path: 'inventory', loadComponent: () => import('./features/officer/inventory/inventory.component').then(m => m.InventoryComponent) },
      { path: 'low-stock', loadComponent: () => import('./features/officer/low-stock/low-stock.component').then(m => m.LowStockComponent) },
    ],
  },

  // ── Admin dashboard ──────────────────────────────────────────────
  {
    path: 'admin',
    canActivate: [roleGuard],
    data: { roles: ['R_HORTIC_ADM', 'ADMIN'] },
    loadComponent: () => import('./features/admin/admin-layout/admin-layout.component')
      .then(m => m.AdminLayoutComponent),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
      { path: 'dashboard', loadComponent: () => import('./features/admin/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'parks', loadComponent: () => import('./features/admin/parks/parks.component').then(m => m.ParksComponent) },
      { path: 'zones', loadComponent: () => import('./features/admin/zones/zones.component').then(m => m.ZonesComponent) },
      { path: 'officials', loadComponent: () => import('./features/admin/officials/officials.component').then(m => m.OfficialsComponent) },
      { path: 'assignments', loadComponent: () => import('./features/admin/assignments/assignments.component').then(m => m.AssignmentsComponent) },
      { path: 'trees', loadComponent: () => import('./features/admin/trees/trees.component').then(m => m.TreesComponent) },
      { path: 'occasions', loadComponent: () => import('./features/admin/occasions/occasions.component').then(m => m.OccasionsComponent) },
      { path: 'inventory', loadComponent: () => import('./features/admin/master-inventory/master-inventory.component').then(m => m.MasterInventoryComponent) },
      { path: 'citizens', loadComponent: () => import('./features/admin/citizens/citizens.component').then(m => m.CitizensComponent) },
      { path: 'bookings', loadComponent: () => import('./features/admin/bookings/bookings.component').then(m => m.BookingsComponent) },
    ],
  },

  { path: 'unauthorized', loadComponent: () => import('./features/auth/unauthorized/unauthorized.component').then(m => m.UnauthorizedComponent) },

  { path: '**', redirectTo: 'home' },
];
