import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { environment } from '../../../../environments/environment';
import { AuthService } from '../../../core/services/auth.service';
import { ForsaLogoComponent } from '../../../shared/branding/forsa-logo.component';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import { ForsaPasswordFieldComponent } from '../../../shared/ui/forsa-password-field/forsa-password-field.component';

@Component({
  selector: 'app-register',
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
  templateUrl: './register.component.html',
  styleUrl: './register.component.css',
})
export class RegisterComponent {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  firstName = '';
  lastName = '';
  username = '';
  email = '';
  password = '';
  termsAccepted = false;

  readonly busy = signal(false);
  readonly googleBusy = signal(false);
  readonly error = signal<string | null>(null);
  readonly success = signal<string | null>(null);

  signUpWithGoogle(): void {
    this.googleBusy.set(true);
    this.error.set(null);
    this.auth.getGoogleLoginUrl().subscribe({
      next: (res) => {
        window.location.href = res.message;
      },
      error: (e) => {
        this.error.set(e.error?.message ?? 'Could not start Google sign-up.');
        this.googleBusy.set(false);
      },
    });
  }
  submit(): void {
    if (!this.termsAccepted) {
      this.error.set('Please accept the terms to continue.');
      return;
    }
    const u = this.username.trim();
    const mail = this.email.trim();
    if (!u || !mail || this.password.length < 6) {
      this.error.set('Username, email and a password (6+ characters) are required.');
      return;
    }
    this.busy.set(true);
    this.error.set(null);
    this.auth
      .signup({
        username: u,
        email: mail,
        password: this.password,
        idrole: environment.defaultClientRoleId,
      })
      .subscribe({
        next: (res) => {
          this.success.set(res.message);
          this.busy.set(false);
          setTimeout(() => void this.router.navigateByUrl('/login'), 2000);
        },
        error: (e) => {
          this.error.set(e.error?.message ?? 'Registration failed.');
          this.busy.set(false);
        },
      });
  }
}