import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { TokenStore } from './token.store';

/** Blocks anonymous users; redirects to /login. */
export const authGuard: CanActivateFn = () => {
  const store = inject(TokenStore);
  const router = inject(Router);
  return store.isAuthenticated ? true : router.createUrlTree(['/login']);
};

/**
 * Role-based guard. Usage: `canActivate: [roleGuard], data: { roles: ['ADMIN'] }`
 * `ADMIN` = R_HORTIC_ADM, `OFFICER` = R_HORTIC_OFF, `SUPERVISOR` = SUPERVISOR,
 * `CITIZEN` = any citizen token.
 */
export const roleGuard: CanActivateFn = (route) => {
  const store = inject(TokenStore);
  const router = inject(Router);

  if (!store.isAuthenticated) return router.createUrlTree(['/login']);

  const allowed = (route.data?.['roles'] as string[] | undefined) ?? [];
  const role = store.role;

  if (allowed.length === 0) return true;

  // Citizens: any token whose role is not an MCD staff role.
  if (allowed.includes('CITIZEN')) {
    const staff = ['R_HORTIC_ADM', 'R_HORTIC_OFF', 'SUPERVISOR', 'ADMIN', 'OFFICER'];
    if (!staff.includes(role)) return true;
  }
  if (allowed.includes(role)) return true;

  return router.createUrlTree(['/unauthorized']);
};
