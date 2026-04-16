import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { ForsaLogoComponent } from '../../../shared/branding/forsa-logo.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import { ForsaPasswordFieldComponent } from '../../../shared/ui/forsa-password-field/forsa-password-field.component';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    FormsModule,
    RouterLink,
    ForsaLogoComponent,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaInputDirective,
    ForsaPasswordFieldComponent,
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css',
})
export class LoginComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  username = '';
  password = '';

  readonly busy = signal(false);
  readonly googleBusy = signal(false);
  readonly error = signal<string | null>(null);

  signInWithGoogle(): void {
    this.googleBusy.set(true);
    this.error.set(null);
    this.auth.getGoogleLoginUrl().subscribe({
      next: (res) => {
        window.location.href = res.message;
      },
      error: (e) => {
        this.error.set(e.error?.message ?? 'Could not start Google sign-in.');
        this.googleBusy.set(false);
      },
    });
  }
  submit(): void {
    if (!this.username.trim() || !this.password) {
      this.error.set('Please enter your username and password.');
      return;
    }
    this.busy.set(true);
    this.error.set(null);
    this.auth.login(this.username.trim(), this.password).subscribe({
      next: () => {
        void this.router.navigateByUrl('/dashboard');
        this.busy.set(false);
      },
      error: (e) => {
        this.error.set(e.error?.message ?? 'Sign in failed.');
        this.busy.set(false);
      },
    });
  }
}