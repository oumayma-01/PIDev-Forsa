import { Component, OnDestroy, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { of } from 'rxjs';
import { catchError, switchMap } from 'rxjs/operators';
import type { CurrentUser } from '../../../core/models/auth.model';
import { AuthService } from '../../../core/services/auth.service';
import { ProfileService } from '../../../core/services/profile.service';
import { ForsaButtonComponent } from '../../../shared/ui/forsa-button/forsa-button.component';
import { ForsaCardComponent } from '../../../shared/ui/forsa-card/forsa-card.component';
import { ForsaIconComponent } from '../../../shared/ui/forsa-icon/forsa-icon.component';
import { ForsaInputDirective } from '../../../shared/directives/forsa-input.directive';
import { ForsaPasswordFieldComponent } from '../../../shared/ui/forsa-password-field/forsa-password-field.component';

@Component({
  selector: 'app-profile-page',
  standalone: true,
  imports: [
    FormsModule,
    ForsaButtonComponent,
    ForsaCardComponent,
    ForsaIconComponent,
    ForsaInputDirective,
    ForsaPasswordFieldComponent,
  ],
  templateUrl: './profile-page.component.html',
  styleUrl: './profile-page.component.css',
})
export class ProfilePageComponent implements OnDestroy {
  private readonly profileApi = inject(ProfileService);
  private readonly auth = inject(AuthService);

  username = '';
  email = '';

  currentPassword = '';
  newPassword = '';
  confirmPassword = '';

  readonly busy = signal(false);
  readonly message = signal<string | null>(null);
  readonly error = signal<string | null>(null);
  readonly avatarPreviewUrl = signal<string | null>(null);
  readonly hasProfileImage = signal(false);
  /** Google-only account (no known password) until user sets one here. */
  readonly oauthGoogleOnly = signal(false);

  /** Prefer live session after password change; fall back to profile load. */
  readonly oauthGoogleOnlyUi = computed(
    () => this.auth.currentUser()?.oauthAccount ?? this.oauthGoogleOnly(),
  );

  private objectUrl: string | null = null;

  constructor() {
    this.reloadProfile();
  }

  ngOnDestroy(): void {
    this.revokeObjectUrl();
  }

  onAvatarSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';
    if (!file) {
      return;
    }
    if (!file.type.startsWith('image/')) {
      this.flashError('Please select an image file.');
      return;
    }
    if (file.size > 2 * 1024 * 1024) {
      this.flashError('Image must be 2 MB or smaller.');
      return;
    }
    this.busy.set(true);
    this.clearAlerts();
    this.profileApi.uploadAvatar(file).subscribe({
      next: (u) => {
        this.applyUser(u);
        this.auth.refreshCurrentUser().subscribe();
        this.refreshAvatarPreview();
        this.flashMessage('Profile photo updated.');
        this.busy.set(false);
      },
      error: (err) => {
        this.busy.set(false);
        this.flashError(this.readError(err));
      },
    });
  }

  removeAvatar(): void {
    this.busy.set(true);
    this.clearAlerts();
    this.profileApi.deleteAvatar().subscribe({
      next: (u) => {
        this.applyUser(u);
        this.auth.refreshCurrentUser().subscribe();
        this.revokeObjectUrl();
        this.avatarPreviewUrl.set(null);
        this.flashMessage('Profile photo removed.');
        this.busy.set(false);
      },
      error: (err) => {
        this.busy.set(false);
        this.flashError(this.readError(err));
      },
    });
  }

  saveProfile(): void {
    this.clearAlerts();
    const u = this.username.trim();
    const e = this.email.trim();
    if (u.length < 3) {
      this.flashError('Username must be at least 3 characters.');
      return;
    }
    if (!e.includes('@')) {
      this.flashError('Please enter a valid email address.');
      return;
    }
    this.busy.set(true);
    this.profileApi.updateProfile({ username: u, email: e }).subscribe({
      next: (user) => {
        this.applyUser(user);
        this.auth.refreshCurrentUser().subscribe();
        this.flashMessage('Profile saved.');
        this.busy.set(false);
      },
      error: (err) => {
        this.busy.set(false);
        this.flashError(this.readError(err));
      },
    });
  }

  savePassword(): void {
    this.clearAlerts();
    if (this.newPassword.length < 6) {
      this.flashError('New password must be at least 6 characters.');
      return;
    }
    if (this.newPassword !== this.confirmPassword) {
      this.flashError('New password and confirmation do not match.');
      return;
    }
    const googleOnly = this.oauthGoogleOnlyUi();
    if (!googleOnly && !this.currentPassword.trim()) {
      this.flashError('Please enter your current password.');
      return;
    }
    this.busy.set(true);
    const payload = googleOnly
      ? { newPassword: this.newPassword }
      : { currentPassword: this.currentPassword, newPassword: this.newPassword };
    this.profileApi.changePassword(payload).subscribe({
      next: (msg) => {
        this.currentPassword = '';
        this.newPassword = '';
        this.confirmPassword = '';
        this.flashMessage(msg.message || 'Password updated.');
        this.auth.refreshCurrentUser().subscribe({
          next: (u) => {
            this.applyUser(u);
            this.busy.set(false);
          },
          error: () => {
            this.oauthGoogleOnly.set(false);
            this.busy.set(false);
          },
        });
      },
      error: (err) => {
        this.busy.set(false);
        this.flashError(this.readError(err));
      },
    });
  }

  private reloadProfile(): void {
    this.clearAlerts();
    this.profileApi
      .getProfile()
      .pipe(
        switchMap((u) => {
          this.applyUser(u);
          if (u.hasProfileImage) {
            return this.profileApi.getAvatarBlob().pipe(
              catchError(() => of(null)),
            );
          }
          return of(null);
        }),
      )
      .subscribe({
        next: (blob) => {
          if (blob && blob.size > 0) {
            this.revokeObjectUrl();
            this.objectUrl = URL.createObjectURL(blob);
            this.avatarPreviewUrl.set(this.objectUrl);
          }
        },
        error: (err) => this.flashError(this.readError(err)),
      });
  }

  private refreshAvatarPreview(): void {
    if (!this.hasProfileImage()) {
      this.revokeObjectUrl();
      this.avatarPreviewUrl.set(null);
      return;
    }
    this.profileApi.getAvatarBlob().subscribe({
      next: (blob) => {
        if (blob.size === 0) {
          return;
        }
        this.revokeObjectUrl();
        this.objectUrl = URL.createObjectURL(blob);
        this.avatarPreviewUrl.set(this.objectUrl);
      },
      error: () => {
        /* ignore */
      },
    });
  }

  private applyUser(u: Pick<CurrentUser, 'username' | 'email' | 'hasProfileImage' | 'oauthAccount'>): void {
    this.username = u.username;
    this.email = u.email;
    this.hasProfileImage.set(!!u.hasProfileImage);
    this.oauthGoogleOnly.set(!!u.oauthAccount);
  }

  private revokeObjectUrl(): void {
    if (this.objectUrl) {
      URL.revokeObjectURL(this.objectUrl);
      this.objectUrl = null;
    }
  }

  private clearAlerts(): void {
    this.message.set(null);
    this.error.set(null);
  }

  private flashMessage(text: string): void {
    this.message.set(text);
  }

  private flashError(text: string): void {
    this.error.set(text);
  }

  private readError(err: unknown): string {
    const body = (err as { error?: { message?: string } })?.error;
    if (body?.message) {
      return body.message;
    }
    return 'Something went wrong. Please try again.';
  }
}
