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
  selector: 'app-officer-layout',
  standalone: true,
  imports: [
    CommonModule, RouterOutlet, RouterLink, RouterLinkActive,
    MatSidenavModule, MatToolbarModule, MatListModule, MatIconModule,
    MatButtonModule, MatMenuModule,
  ],
  templateUrl: './officer-layout.component.html',
  styleUrl: './officer-layout.component.css',
})
export class OfficerLayoutComponent {
  readonly auth = inject(AuthService);
  private readonly bp = inject(BreakpointObserver);

  readonly isHandset = toSignal(
    this.bp.observe(Breakpoints.Handset).pipe(map(r => r.matches)),
    { initialValue: false }
  );

  readonly nav = [
    { path: '/officer/dashboard', label: 'Dashboard', icon: 'dashboard' },
    { path: '/officer/pending', label: 'Pending Plantations', icon: 'pending_actions' },
    { path: '/officer/completed', label: 'Completed', icon: 'task_alt' },
    { path: '/officer/slots', label: 'Manage Slots', icon: 'event' },
    { path: '/officer/inventory', label: 'Park Inventory', icon: 'inventory' },
    { path: '/officer/low-stock', label: 'Low Stock Alerts', icon: 'report' },
  ];
}
