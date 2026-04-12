import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ForsaLogoComponent } from '../../../shared/branding/forsa-logo.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    ForsaLogoComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaInputDirective,
  ],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.css',
})
export class ForgotPasswordComponent {
  private readonly auth = inject(AuthService);

  email = '';

  readonly busy = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);

  submit(): void {
    const mail = this.email.trim();
    if (!mail) {
      this.error.set('Please enter your email address.');
      return;
    }
    this.busy.set(true);
    this.error.set(null);
    this.success.set(null);
    this.auth.requestPasswordReset(mail).subscribe({
      next: (res) => {
        this.success.set(res.message);
        this.busy.set(false);
      },
      error: (e) => {
        this.error.set(e.error?.message ?? 'Could not send reset email.');
        this.busy.set(false);
      },
    });
  }
}
