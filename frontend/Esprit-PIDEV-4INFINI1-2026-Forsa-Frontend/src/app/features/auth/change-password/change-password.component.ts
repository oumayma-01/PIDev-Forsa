import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { environment } from '../../../../environments/environment';
import type { MessageResponse } from '../../../core/models/auth.model';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';

@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [FormsModule, RouterLink, ForsaCardComponent, ForsaButtonComponent, ForsaInputDirective],
  templateUrl: './change-password.component.html',
  styleUrl: './change-password.component.css',
})
export class ChangePasswordComponent {
  private readonly http = inject(HttpClient);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  password = '';
  confirm = '';

  readonly busy = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);

  submit(): void {
    const token = this.route.snapshot.paramMap.get('token');
    const emailCipher = this.route.snapshot.paramMap.get('email');
    if (!token || !emailCipher) {
      this.error.set('Invalid reset link.');
      return;
    }
    if (this.password.length < 6) {
      this.error.set('Password must be at least 6 characters.');
      return;
    }
    if (this.password !== this.confirm) {
      this.error.set('Passwords do not match.');
      return;
    }
    this.busy.set(true);
    this.error.set(null);
    this.http
      .post<MessageResponse>(`${environment.apiBaseUrl}/auth/resetpass`, {
        token,
        email: emailCipher,
        password: this.password,
      })
      .subscribe({
        next: (res) => {
          this.success.set(res.message);
          this.busy.set(false);
          setTimeout(() => void this.router.navigateByUrl('/login'), 2000);
        },
        error: (e) => {
          this.error.set(e.error?.message ?? 'Reset failed.');
          this.busy.set(false);
        },
      });
  }
}
