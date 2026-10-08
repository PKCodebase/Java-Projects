import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { TokenStore } from '../auth/token.store';

/** Attaches the Bearer JWT to every API call and handles 401 by logging out. */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const store = inject(TokenStore);
  const router = inject(Router);
  const token = store.token;

  const authReq = token
    ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : req;

  return next(authReq).pipe(
    catchError((err: HttpErrorResponse) => {
      if (err.status === 401) {
        store.clear();
        router.navigate(['/login']);
      }
      return throwError(() => err);
    })
  );
};
