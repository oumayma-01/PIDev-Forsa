import { Component, inject, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-oauth-success',
  standalone: true,
  templateUrl: './oauth-success.component.html',
  styleUrl: './oauth-success.component.css',
})
export class OAuthSuccessComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly auth = inject(AuthService);

  readonly status = signal<'loading' | 'error'>('loading');
  readonly message = signal<string>('Signing you in…');

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');
    if (!token?.trim()) {
      this.status.set('error');
      this.message.set('Missing token. Please try signing in with Google again.');
      return;
    }
    this.auth.completeOAuthSession(token.trim()).subscribe({
      next: () => {
        void this.router.navigateByUrl('/dashboard');
      },
      error: () => {
        this.auth.clearSession();
        this.status.set('error');
        this.message.set('Could not complete sign-in. Please try again.');
      },
    });
  }

  goToLogin(): void {
    void this.router.navigateByUrl('/login');
  }
}
