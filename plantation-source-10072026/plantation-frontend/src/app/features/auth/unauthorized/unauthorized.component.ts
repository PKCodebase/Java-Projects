import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-unauthorized',
  standalone: true,
  imports: [RouterLink, MatButtonModule],
  template: `
    <div class="wrap">
      <h1>403</h1>
      <p>You don't have permission to view this page.</p>
      <a mat-flat-button color="primary" routerLink="/home">Go home</a>
    </div>
  `,
  styles: [`.wrap{min-height:100vh;display:grid;place-content:center;text-align:center;gap:12px}
            h1{font-size:4rem;margin:0;color:#2e7d32} p{color:rgba(0,0,0,.6)}`],
})
export class UnauthorizedComponent {}
