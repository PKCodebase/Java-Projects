import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatListModule } from '@angular/material/list';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { map } from 'rxjs';
import { toSignal } from '@angular/core/rxjs-interop';
import { AuthService } from '../../../core/auth/auth.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [
    CommonModule, RouterOutlet, RouterLink, RouterLinkActive,
    MatSidenavModule, MatToolbarModule, MatListModule, MatIconModule,
    MatButtonModule, MatMenuModule,
  ],
  templateUrl: './admin-layout.component.html',
  styleUrl: './admin-layout.component.css',
})
export class AdminLayoutComponent {
  readonly auth = inject(AuthService);
  private readonly bp = inject(BreakpointObserver);

  readonly isHandset = toSignal(
    this.bp.observe(Breakpoints.Handset).pipe(map(r => r.matches)),
    { initialValue: false }
  );

  readonly nav = [
    { path: '/admin/dashboard', label: 'Dashboard', icon: 'dashboard' },
    { path: '/admin/parks', label: 'Parks', icon: 'park' },
    { path: '/admin/zones', label: 'Zones', icon: 'map' },
    { path: '/admin/officials', label: 'Officials', icon: 'badge' },
    { path: '/admin/assignments', label: 'Assignments', icon: 'assignment_ind' },
    { path: '/admin/trees', label: 'Tree Species', icon: 'nature' },
    { path: '/admin/occasions', label: 'Occasions', icon: 'event' },
    { path: '/admin/inventory', label: 'Master Inventory', icon: 'inventory_2' },
    { path: '/admin/citizens', label: 'Citizens', icon: 'groups' },
    { path: '/admin/bookings', label: 'Bookings', icon: 'receipt_long' },
  ];
}
