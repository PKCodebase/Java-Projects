import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ApiService } from '../../../core/http/api.service';
import { Certificate } from '../../../core/models';

@Component({
  selector: 'app-verify',
  standalone: true,
  imports: [
    CommonModule, FormsModule, MatCardModule, MatFormFieldModule,
    MatInputModule, MatButtonModule, MatIconModule,
  ],
  templateUrl: './verify.component.html',
  styleUrl: './verify.component.css',
})
export class VerifyComponent {
  private readonly api = inject(ApiService);
  private readonly route = inject(ActivatedRoute);

  certNumber = '';
  readonly result = signal<Certificate | null>(null);
  readonly error = signal('');
  readonly loading = signal(false);

  constructor() {
    // Support /verify?cert=NUMBER deep-links from the certificates page.
    const preset = this.route.snapshot.queryParamMap.get('cert');
    if (preset) { this.certNumber = preset; this.verify(); }
  }

  verify(): void {
    if (!this.certNumber.trim()) return;
    this.loading.set(true);
    this.error.set('');
    this.result.set(null);
    this.api.verifyCertificate(this.certNumber.trim()).subscribe({
      next: (c) => { this.result.set(c); this.loading.set(false); },
      error: (err) => {
        this.error.set(err?.error?.message ?? 'Certificate not found or invalid');
        this.loading.set(false);
      },
    });
  }
}
