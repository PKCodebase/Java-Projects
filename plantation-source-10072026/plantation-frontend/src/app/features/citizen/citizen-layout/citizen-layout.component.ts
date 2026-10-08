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
  selector: 'app-citizen-layout',
  standalone: true,
  imports: [
    CommonModule, RouterOutlet, RouterLink, RouterLinkActive,
    MatSidenavModule, MatToolbarModule, MatListModule, MatIconModule,
    MatButtonModule, MatMenuModule,
  ],
  templateUrl: './citizen-layout.component.html',
  styleUrl: './citizen-layout.component.css',
})
export class CitizenLayoutComponent {
  readonly auth = inject(AuthService);
  private readonly bp = inject(BreakpointObserver);

  readonly isHandset = toSignal(
    this.bp.observe(Breakpoints.Handset).pipe(map(r => r.matches)),
    { initialValue: false }
  );

  readonly nav = [
    { path: '/citizen/book', label: 'Book a slot', icon: 'add_circle' },
    { path: '/citizen/dashboard', label: 'Dashboard', icon: 'dashboard' },
    { path: '/citizen/bookings', label: 'My Bookings', icon: 'event' },
    { path: '/citizen/certificates', label: 'My Certificates', icon: 'workspace_premium' },
    { path: '/parks', label: 'Browse Parks', icon: 'park' },
  ];
}
